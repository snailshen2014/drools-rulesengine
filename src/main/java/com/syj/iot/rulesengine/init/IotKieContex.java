
/**
 * 
 */
package com.syj.iot.rulesengine.init;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syj.iot.rulesengine.typedefine.CommandParameter;
import com.syj.iot.rulesengine.typedefine.EngineCommand;
import com.syj.iot.rulesengine.utils.KieUtils;

/**
 * @des:singleton class,by this class can get KieService,KieContainer and so on
 * @author shenyanjun1
 * @date: 2018年5月26日 下午2:25:42
 */
public class IotKieContex {
	private static volatile IotKieContex kieContex;
	private static Logger logger = LoggerFactory.getLogger(IotKieContex.class);

	// iot kie contex status 0:normal 1:recompile
	private volatile IotKieContexEnv kieStatus;
	
	private  IotKieContexEnv sessionStatus;
	// KieService
	private KieServices ks;
	// releaseId
	private ReleaseId releaseId;
	// kJar
	private InternalKieModule kModule;
	private KieRepository kRepo;
	// kie container
	private KieContainer kContainer;

	private IotKieContex() {
	}
	/**
	 * @des double check singleton pattern
	 * @return
	 */
	public static  IotKieContex getIotKieContex() {
		if (kieContex == null) {
			synchronized(IotKieContex.class) {
				if (kieContex == null)
					kieContex = new IotKieContex();
			}
		}
		return kieContex;
	}

	/**
	 * init kie runtime env
	 * @param true:statefulSession, false :stateless session
	 * @throws IOException
	 */
	public synchronized void  initIotKieContex(boolean statefulSession) throws Exception {
		setKieStatus(IotKieContexEnv.BUSY);
		setSessionStatus(statefulSession == true ? IotKieContexEnv.STATEFULE_SESSION : IotKieContexEnv.STATELESS_SESSION);
		logger.info("RulesEngine#### init IotKieContex begin.");
		ks = KieServices.Factory.get();
		releaseId = ks.newReleaseId("com.syj.iot", "rulesengine", "1.0.0");
		kModule = KieUtils.initKieJar(ks, releaseId,statefulSession);
		kRepo = ks.getRepository();
		kRepo.addKieModule(kModule);
		kContainer = ks.newKieContainer(releaseId); 
		setKieStatus(IotKieContexEnv.IDLE);
		logger.info("RulesEngine#### init IotKieContex end.");
	}

	public KieContainer getKieContainer() {
		return kContainer;
	}
	
	/**
	 * 
	 * @return
	 */
	public StatelessKieSession getJobSession() {
		return kContainer.getKieBase().newStatelessKieSession();
	}
	/**
	 * 
	 * @return
	 */
	public StatelessKieSession getJobSession(Long formatId) {
//		System.out.println("RulesEngine ###### get session,parameter:" + "kbase" + formatId);
		return kContainer.getKieBase("kbase" + formatId).newStatelessKieSession();

	}
	
	public KieSession getJobStatefulSession(Long formatId) {
//		System.out.println("RulesEngine ###### get session,parameter:" + "kbase" + formatId);
		return kContainer.getKieBase("kbase" + formatId).newKieSession();

	}
	/**
	 * @des get KieBase
	 * @param formatId
	 * @return
	 */
	private KieBase getKieBase(Long formatId) {
		return kContainer.getKieBase("kbase" + formatId);
	}
	
	public KieSession getJobStatefulSession() {
		return kContainer.getKieBase().newKieSession();

	}
	/**
	 * @des now recreate kjar,next updateToVersion by kcontainer
	 * @param rule
	 * @throws IOException
	 */
	private void updateKieEnv() throws Exception {
		// if init what?now sleep ,
		while (this.getKieStatus() == IotKieContexEnv.BUSY) {
			try {
				Thread.sleep(1000);
				logger.info("RulesEngine#### now init iot kie env ,busi.waitting...");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		initIotKieContex(isSessionStateful());
	}
	
	/**
	 * @des synchronize kie env
	 * @param command
	 * @param param
	 */
	public boolean notifyKieEnv(EngineCommand command,CommandParameter param) {
		logger.info("RulesEngine#### notifyKieEnv  begin,command:{}",command.getMsg());
		boolean flag = false;
		switch (command) {
		case PRODUCT_ONLINE:
		case RULE_ADD:
		case RULE_UPDATE:
		case RULE_DELETE:
		case PRODUCT_MODIFY_DATEFORMAT:
			flag = true;
			break;
		case PRODUCT_OFFLINE:
			break;
		case DEVICE_ONLINE:
			break;
		case DEVICE_OFFLINE:
			break;
		default :
			System.out.println("syncKieEnv command type error:" + command);
			break;
		}
		try {
			if(flag)
				updateKieEnv();
		} catch (Exception e) {
			logger.error("RulesEngine#### notifyKieEnv  error,command:{}",command.getMsg());
			e.printStackTrace();
			setKieStatus(IotKieContexEnv.IDLE);
			return false;
		}
		logger.info("RulesEngine#### notifyKieEnv  end,command:{}",command.getMsg());
		return true;
	}
	
	public KieBase getIotKieBase() {
		return kContainer.getKieBase();
	}
	
	/**
	 * 
	 * @param kieBase
	 */
	public  void explainKieBase(KieBase kieBase) {
        if ( kieBase == null ) return;
        Collection<Process> processes = kieBase.getProcesses();
        if ( processes == null || processes.isEmpty() ) {
        	logger.error("kieBase contained NO processes.");
        } else {
        	logger.info(MessageFormat.format("Found {0,number} processes:", processes.size()));
            for ( Process process : processes ) {
            	logger.info(MessageFormat.format("{0}:{1} - {2} - {3}", 
                    process.getPackageName(), process.getName(), process.getType(), process.getVersion()));
            }
        }
        Collection<KiePackage> packages = kieBase.getKiePackages();
        if ( packages == null || packages.isEmpty() ) {
        	logger.error("kieBase contained NO packages.");
        } else {
        	logger.info(MessageFormat.format("Found {0,number} packages:", packages.size()));
            for ( KiePackage p : packages ) {
            	logger.info(MessageFormat.format("{0}", p.getName()));
                Collection<Rule> rules = p.getRules();
                if ( rules == null || rules.isEmpty() ) {
                	logger.error("kieBase:package contained NO rules.");
                } else {
                	for ( Rule rule : rules ) {
                        	logger.info(MessageFormat.format("Rule {0}:{1}", 
                                rule.getPackageName(), rule.getName())); 
                    }
                    
                }
            }
        }
    }
	
	public boolean isIotKieBusy() {
		return kieStatus == IotKieContexEnv.BUSY ? true : false;
	}
	
	/**
	 * 
	 * @des:kie contex system status
	 * @author shenyanjun1
	 * @date: 2018年7月26日 下午2:38:27
	 */
	private enum IotKieContexEnv {
		BUSY(1, "IoT kie env busying,recreating."),
		IDLE(0, "IoT kie env idle,can using."),
		STATEFULE_SESSION(2,"Now using Kie stateful session."),
		STATELESS_SESSION(3,"Now using Kie stateless session.");
		/**
		 * @return the status
		 */
		public int getStatus() {
			return status;
		}

		/**
		 * @param status the status to set
		 */
		public void setStatus(int status) {
			this.status = status;
		}

		/**
		 * @return the msg
		 */
		public String getMsg() {
			return msg;
		}

		/**
		 * @param msg the msg to set
		 */
		public void setMsg(String msg) {
			this.msg = msg;
		}

		private int status;
		private String msg;

		private IotKieContexEnv(int status, String msg) {
			this.status = status;
			this.msg = msg;
		}

	}

	/**
	 * @return the kieStatus
	 */
	public IotKieContexEnv getKieStatus() {
		return kieStatus;
	}

	/**
	 * @param kieStatus the kieStatus to set
	 */
	public void setKieStatus(IotKieContexEnv kieStatus) {
		this.kieStatus = kieStatus;
	}
	/**
	 * @return the sessionStatus
	 */
	public  boolean isSessionStateful() {
		return sessionStatus == IotKieContexEnv.STATEFULE_SESSION ? true : false;
	}

	/**
	 * @param sessionStatus the sessionStatus to set
	 */
	public  void setSessionStatus(IotKieContexEnv sessionType) {
		sessionStatus = sessionType;
	}

}
