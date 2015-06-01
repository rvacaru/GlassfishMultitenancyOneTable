/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clickreply.fw5.glassfishmultitenancy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.ejb.Singleton;

/**
 * Tried to use SessionContext for sharing currentTenant info but it seems it's not propagated to other EJBs and you need an Interceptor to enrich 
 * the context with the Tenant: 
 * http://stackoverflow.com/questions/21886158/sharing-data-across-multiple-ejbs-using-context.
 * http://stackoverflow.com/questions/7887182/custom-principal-wont-be-propagated-to-ejb-sessioncontext-on-jboss-as
 * 
 * Ejb's injection are not working with the arquillian deployment 
 * https://java.net/jira/browse/GLASSFISH-21167
 * http://stackoverflow.com/questions/29963705/arquillian-cant-inject-a-local-or-remote-ejb-on-glassfish-embedded-container
 * @author r.vacaru
 */

@Singleton
public class ContextHolderBean implements Serializable {

    private Map<String, Object> ctxData;

    public ContextHolderBean() {
        this.ctxData = new HashMap<>();
    }

    public Map<String, Object> getContextData() {
        return Objects.requireNonNull(ctxData, "ContextData map is null");
    }

    public Object get(String key) {
        return ctxData.get(key);
    }

    public void put(String key, Object value) {
        ctxData.put(key, value);
    }

    public Object replace(String key, Object value) {
        return ctxData.replace(key, value);
    }

    public Object putOrReplace(String key, Object value) {
        if (ctxData.containsKey(key)) {
            return replace(key, value);
        } else {
            put(key, value);
            return value;
        }
    }
}
