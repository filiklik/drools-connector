package org.mule.modules.drools;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.mule.RequestContext;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.Path;
import com.google.gson.Gson;

/**
 * 
 * @author Kishan, Monica 
 * @Date   September 5th , 2016
 */
@Connector(name="drools", friendlyName="Drools")
public class DroolsConnector {

	/**
     *  Drools is a Business Logic integration Platform.It is a collection of tools that allow us to 
     *  separate and reason over logic and data found within business processes.
     *  Drools is a Rule Engine that uses the rule-based approach to implement and Expert System. 
     *  The Rule Engine matches facts and data against Production Rules to infer conclusions which result in actions.  
     *  A Rule Engine allows you to define "What to Do" and not "How to do it."
     *  Rules are pieces of knowledge often expressed as, "When some conditions occur, then do some tasks."
     *   
     *  Input for the connector is a DRL file containing rules and Data on which rules has to be applied.
     *   
     * @param filePath path of DRL file containing rules to be executed
     * @return message containing JSON object when success and corresponding error message for Exception.
     */
    @Processor(friendlyName="Execute Rules")
    public String execute(@Path String filePath) {
    	try{ 
        	System.out.println("filePath-----" + filePath); 
        	Gson gson = new Gson();
        	ArrayList objectArray= null;
        	//Object object= new Object();
        	List<Object> resultList = new ArrayList<>();
        	if ( RequestContext.getEventContext( ) != null && RequestContext.getEventContext( ).getMessage( ) != null ){
	        	System.out.println(RequestContext.getEventContext().getMessage().getPayload());
	        	objectArray =(ArrayList) RequestContext.getEventContext().getMessage().getPayload();
	        	for (Object object : objectArray) {
	        		try {
	            		System.out.println("request object===" +gson.toJson(object));  
	    			} catch (Exception e) {
	    				throw new IllegalArgumentException("Input are not getting read properly");
	    			}
	            	KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
	    	        kbuilder.add(ResourceFactory.newInputStreamResource(new FileInputStream(new File(filePath))), ResourceType.DRL);
	    	        KnowledgeBuilderErrors errors = kbuilder.getErrors();
	    	        Boolean errorDescFlag=true;
	    	        if(errors.size() > 0){
	    	        	for (KnowledgeBuilderError error: errors) {
	    	                System.err.println(error);
	    	                if(error.getMessage().contains("Rule Compilation error"))
	    	                	errorDescFlag=false;
	    	        		}
	    	        	if(!errorDescFlag)
	    	        		throw new IllegalArgumentException("Rule File Syntax error");
	    	        	else
	    	        		throw new IllegalArgumentException("DRL File Error");
	    	        }
	    	        
	    	        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(); 
	    	        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
	    	        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();	
	    	        ksession.insert(object);
	    	        ksession.fireAllRules();
	    	        System.out.println("After firing rules");
	    	       // List<Object> resultList = new ArrayList<>();
	    	        if(ksession.getObjects().size() > 1){
	    	        	for(Object resobj: ksession.getObjects()){
	    		        	System.out.println("object"+object.toString()+"...resobj"+resobj.toString());
	    	        		if(resobj != object){
	    		        		System.out.println("in the if condition"+resobj.toString());
	    	        			resultList.add(resobj);
	    		        	}
	    		        }
	    	        }
	    	        
	    	        System.out.println(gson.toJson(resultList));
				}
	        	 
        	}else{
        		throw new IllegalArgumentException( "Cannot access context implicitly" );
        	}
        	
	       return gson.toJson(resultList);
        }catch(Exception e){
        	e.printStackTrace();
        	return "ERROR OCCURED: " +e.getMessage();
        }
    }

}