/**
 * 
 */
package com.syj.iot.rulesengine.init;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @des:RulesEngine init,load config,start Cache,listener etc
 * @author shenyanjun1
 * @date: 2018年5月22日 下午2:36:52
 */

@Component
public class IotRulesInit implements  CommandLineRunner{
	@Autowired
	private IotRulesConfigurationService ioTRulesService;

	@Override
	public void run(String... args) throws Exception {
		// add rules from db
//		if (!ioTRulesService.init()) {
//			System.out.println("RulesEngine system load rules error,system will be exit.");
//			System.exit(-1);
//		}
		// init kie runtime env
		IotKieContex iotKieContex = IotKieContex.getIotKieContex();
		try {
			iotKieContex.initIotKieContex(false);
		} catch (Exception e) {
			System.out.println("RulesEngine system init rules runtime env error,system will be exit.");
			System.exit(-1);
		}
		
		System.out.println("RulesEngine rules init ok.");
	}

}
