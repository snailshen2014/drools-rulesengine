/**
 * 
 */
package com.syj.iot.rulesengine.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Calendars;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.LiveQuery;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.ViewChangedEventListener;
import org.kie.api.time.SessionClock;

import com.syj.iot.rulesengine.init.IotKieContex;

public class IoTJobSession   implements KieSession, StatelessKieSession {
	private KieSession kieSession;
    private StatelessKieSession statelessKieSession;
    private final List<?> commands;
    
	private IotKieContex contex;
	private Long formatId;
    public IoTJobSession(IotKieContex contex,Long formatId,List<?> commands) {
    	this.contex = contex;
    	this.formatId = formatId;
    	this.commands = commands;
    }

    private   Object getSession() {
        boolean stateful = contex.isSessionStateful();
        if ( stateful ) {
        	return kieSession =  contex.getJobStatefulSession(this.formatId);
        } else {
        	return statelessKieSession = contex.getJobSession(this.formatId);
        }
  
    }
    private boolean isStateless() {
        return !contex.isSessionStateful();
    }

    private KieSession getKieSession() {
        return ( (KieSession) getSession() );
    }

    private StatelessKieSession getStatelessKieSession() {
        return ( (StatelessKieSession) getSession() );
    }
    @Override
    public int getId() {
        return getKieSession().getId();
    }

    @Override
    public long getIdentifier() {
        return getKieSession().getIdentifier();
    }

    @Override
    public void dispose() {
        getKieSession().dispose();
    }

    @Override
    public void destroy() {
        getKieSession().destroy();
    }

    @Override
    public <T> T execute( Command<T> command ) {
        return ( (CommandExecutor) getSession() ).execute( command );
    }

    @Override
    public <T extends SessionClock> T getSessionClock() {
        return getKieSession().getSessionClock();
    }

    @Override
    public void setGlobal( String identifier, Object value ) {
        if (isStateless()) {
            getStatelessKieSession().setGlobal( identifier, value );
        } else {
            getKieSession().setGlobal( identifier, value );
        }
    }

    @Override
    public Object getGlobal( String identifier ) {
        return getKieSession().getGlobal( identifier );
    }

    @Override
    public Globals getGlobals() {
        if (isStateless()) {
            return getStatelessKieSession().getGlobals( );
        } else {
            return getKieSession().getGlobals( );
        }
    }

    @Override
    public Calendars getCalendars() {
        return getKieSession().getCalendars();
    }

    @Override
    public Environment getEnvironment() {
        return getKieSession().getEnvironment();
    }

    @Override
    public KieBase getKieBase() {
        if (isStateless()) {
            return getStatelessKieSession().getKieBase( );
        } else {
            return getKieSession().getKieBase( );
        }
    }

    @Override
    public void registerChannel( String name, Channel channel ) {
        if (isStateless()) {
            getStatelessKieSession().registerChannel( name, channel );
        } else {
            getKieSession().registerChannel( name, channel );
        }
    }

    @Override
    public void unregisterChannel( String name ) {
        if (isStateless()) {
            getStatelessKieSession().unregisterChannel( name );
        } else {
            getKieSession().unregisterChannel( name );
        }
    }

    @Override
    public Map<String, Channel> getChannels() {
        if (isStateless()) {
            return getStatelessKieSession().getChannels( );
        } else {
            return getKieSession().getChannels( );
        }
    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
        return getKieSession().getSessionConfiguration();
    }

    @Override
    public void halt() {
        getKieSession().halt();
    }

    @Override
    public Agenda getAgenda() {
        return getKieSession().getAgenda();
    }

    @Override
    public EntryPoint getEntryPoint( String name ) {
        return getKieSession().getEntryPoint( name );
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        return getKieSession().getEntryPoints();
    }

    @Override
    public QueryResults getQueryResults( String query, Object... arguments ) {
        return getKieSession().getQueryResults( query, arguments );
    }

    @Override
    public LiveQuery openLiveQuery( String query, Object[] arguments, ViewChangedEventListener listener ) {
        return getKieSession().openLiveQuery( query, arguments, listener );
    }

    @Override
    public String getEntryPointId() {
        return getKieSession().getEntryPointId();
    }

    @Override
    public FactHandle insert( Object object ) {
        return getKieSession().insert( object );
    }

    @Override
    public void retract( FactHandle handle ) {
        getKieSession().retract( handle );
    }

    @Override
    public void delete( FactHandle handle ) {
        getKieSession().delete( handle );
    }

    @Override
    public void delete( FactHandle handle, FactHandle.State fhState ) {
        getKieSession().delete( handle, fhState );
    }

    @Override
    public void update( FactHandle handle, Object object ) {
        getKieSession().update( handle, object );
    }

    @Override
    public void update( FactHandle handle, Object object, String... modifiedProperties ) {
        getKieSession().update( handle, object, modifiedProperties );
    }

    @Override
    public FactHandle getFactHandle( Object object ) {
        return getKieSession().getFactHandle( object );
    }

    @Override
    public Object getObject( FactHandle factHandle ) {
        return getKieSession().getObject( factHandle );
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return getKieSession().getObjects();
    }

    @Override
    public Collection<? extends Object> getObjects( ObjectFilter filter ) {
        return getKieSession().getObjects(filter);
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return getKieSession().getFactHandles();
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles( ObjectFilter filter ) {
        return getKieSession().getFactHandles(filter);
    }

    @Override
    public long getFactCount() {
        return getKieSession().getFactCount();
    }

    @Override
    public KieRuntimeLogger getLogger() {
        return getKieSession().getLogger();
    }

    @Override
    public void addEventListener( ProcessEventListener listener ) {
        getKieSession().addEventListener( listener );
    }

    @Override
    public void removeEventListener( ProcessEventListener listener ) {
        getKieSession().removeEventListener( listener );
    }

    @Override
    public Collection<ProcessEventListener> getProcessEventListeners() {
        return getKieSession().getProcessEventListeners();
    }

    @Override
    public ProcessInstance startProcess( String processId ) {
        return getKieSession().startProcess( processId );
    }

    @Override
    public ProcessInstance startProcess( String processId, Map<String, Object> parameters ) {
        return getKieSession().startProcess( processId, parameters );
    }

    @Override
    public ProcessInstance createProcessInstance( String processId, Map<String, Object> parameters ) {
        return getKieSession().createProcessInstance( processId, parameters );
    }

    @Override
    public ProcessInstance startProcessInstance( long processInstanceId ) {
        return getKieSession().startProcessInstance( processInstanceId );
    }

    @Override
    public void signalEvent( String type, Object event ) {
        getKieSession().signalEvent( type, event );
    }

    @Override
    public void signalEvent( String type, Object event, long processInstanceId ) {
        getKieSession().signalEvent( type, event, processInstanceId );
    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return getKieSession().getProcessInstances();
    }

    @Override
    public ProcessInstance getProcessInstance( long processInstanceId ) {
        return getKieSession().getProcessInstance( processInstanceId );
    }

    @Override
    public ProcessInstance getProcessInstance( long processInstanceId, boolean readonly ) {
        return getKieSession().getProcessInstance( processInstanceId, readonly );
    }

    @Override
    public void abortProcessInstance( long processInstanceId ) {
        getKieSession().abortProcessInstance( processInstanceId );
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        return getKieSession().getWorkItemManager();
    }

    @Override
    public void addEventListener( RuleRuntimeEventListener listener ) {
        getKieSession().addEventListener( listener );
    }

    @Override
    public void removeEventListener( RuleRuntimeEventListener listener ) {
        getKieSession().removeEventListener( listener );
    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return getKieSession().getRuleRuntimeEventListeners();
    }

    @Override
    public void addEventListener( AgendaEventListener listener ) {
        getKieSession().addEventListener( listener );
    }

    @Override
    public void removeEventListener( AgendaEventListener listener ) {
        getKieSession().removeEventListener( listener );
    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return getKieSession().getAgendaEventListeners();
    }

    @Override
    public int fireAllRules() {
        return getKieSession().fireAllRules();
    }

    @Override
    public int fireAllRules( int max ) {
        return getKieSession().fireAllRules(max);
    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter ) {
        return getKieSession().fireAllRules(agendaFilter);
    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter, int max ) {
        return getKieSession().fireAllRules(agendaFilter, max);
    }

    @Override
    public void fireUntilHalt() {
        getKieSession().fireUntilHalt();
    }

    @Override
    public void fireUntilHalt( AgendaFilter agendaFilter ) {
        getKieSession().fireUntilHalt(agendaFilter);
    }
    
    @Override
    public void submit(AtomicAction action) {
        getKieSession().submit( action );
    }

    @Override
    public <T> T getKieRuntime(Class<T> cls) {
        return getKieSession().getKieRuntime( cls );
    }

    @Override
    public void execute( Object object ) {
        getStatelessKieSession().execute( object );
    }

    @Override
    public void execute( Iterable objects ) {
        getStatelessKieSession().execute( objects );
    }
    
}