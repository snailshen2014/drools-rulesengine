/**
 * 
 */
package com.syj.iot.rulesengine.rule;

import org.drools.core.spi.Activation;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

/**
 * @des:only execute the same agenda-group,rule's name
 * @author shenyanjun1
 * @date: 2018年7月11日 上午11:00:38
 */
public class RuleIdEqualsAgendaFilter implements AgendaFilter {
    private final String ruleName;
    public RuleIdEqualsAgendaFilter(final String ruleName) {
        this.ruleName = ruleName;
    }
    public boolean accept(final Activation activation) {
        return activation.getRule().getName().equals(this.ruleName);
    }
	/* (non-Javadoc)
	 * @see org.kie.api.runtime.rule.AgendaFilter#accept(org.kie.api.runtime.rule.Match)
	 */
	@Override
	public boolean accept(Match match) {
		// TODO Auto-generated method stub
		return false;
	}
}
