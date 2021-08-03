package uk.ac.ox_city.logmap_ml.melt;


import de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.MatcherCLI;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process.ExternalProcess;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process.ProcessOutputAlignmentCollector;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the external Python matcher.
 * RDFlib is required in python environment.
 */
public class LogMapML_MELT extends MatcherCLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogMapML_MELT.class);
    
    
    //1. Override matcher (from MatcherURL) too so that LogMap is executed
    //2. Save alignments in URL and give them as input parameter as for the source and target ontology URLs 
    //(we will need to extend MatcherCLI,  add new elements to SubsitiutionMap)
    //3. In python check if you can read ontos and files with mappings, e.g. owlready
    
    
   
    /**
     * The command which should be executed as one string (containing potentially multiple arguments).The line is splitted by whitespace but quotations are respected.
     * An argument line can contain scopes (scopes are only printed if all variables in a scope can be replaced).
     * The following varaibles are replaced:
     * <ul>
     * <li>${source} with source URI</li>
     * <li>${target} with target URI</li>
     * <li>${inputAlignment} with inputAlignment URI</li>
     * <li>${parameters} with parameters URI</li>
     * <li>system properties like ${line.separator} or ${file.separator} ${java.io.tmpdir}</li>
     * <li>environment variables like ${PATH}</li>
     * <li>JVM arguments like ${Xmx} which is replaced by e.g. -Xmx10G</li>
     * </ul>
     * For more see {@link ExternalProcess#addSubstitutionForSystemProperties() }, 
     * {@link ExternalProcess#addSubstitutionForEnvironmentVariables() } and
     * {@link ExternalProcess#addSubstitutionForJVMArguments() }.
     * 
     * The String can also contain scopes which are created with $[...].
     * The scope is only printed if all variables in the scope can be replaced. This is good for named arguments like:
     * $[-i ${inputAlignment}] then only -i is printed if the input alignment is set.
     * @return the string which represents the command to execute.
     * @throws java.lang.Exception in case something goes from when generating the command
     */
    @Override
    protected String getCommand() throws Exception {
        return getPythonCommand() + " oaei-resources${file.separator}pythonMatcher.py ${source} ${target} $[${inputAlignment}] $[${parameters}]";
    }
    
    /**
     * Returns the python command which is extracted from {@code file oaei-resources/python_command.txt}.
     * @return The python executable path.
     */
    protected String getPythonCommand(){
    	//TODO Include this? or already done in image?
        Path filePath = Paths.get("oaei-resources", "python_command.txt");
        if(Files.exists(filePath)){
            try {
                return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8).replace("\r", "").replace("\n", "").trim();
            } catch (IOException ex) { LOGGER.warn("Could not read python command file", ex);}
        }
        return "python";
    }
    
    
    
    /**
     * if set to true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout.
     * if set to false, all logging should go to stdout and the result of the process (url or alignment api format) should go to stderr.
     * @return true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout, false otherwise
     */
    protected boolean isUsingStdOut(){
        return true;
    }

    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        return match(source, target, inputAlignment, null);
    }
    
    
    
    private Map<String,Object> getSubsitiutionMap(URL source, URL target, URL inputAlignment, URL parameters){
    	//TODO To be extended
        Map<String,Object> map = new HashMap<>();
        map.put("source", source);
        map.put("target", target);
        map.put("inputAlignment", inputAlignment);
        map.put("parameters", parameters);
        return map;
    }

    @Override
    public URL match(URL source, URL target, URL inputAlignment, URL parameters) throws Exception {
    	
    	//TODO Call LogMap and get URLs of mappings
    	//Then the rest is given to python via the external process. Then the alignment will be collected from the stdout or stderr	    	
    	
    	
        ExternalProcess p = new ExternalProcess();
        p.addArgumentLine(getCommand());
        p.addSubstitutionMap(getSubsitiutionMap(source, target, inputAlignment, parameters));
        p.addSubstitutionDefaultLookups();
        ProcessOutputAlignmentCollector alignmentCollector = new ProcessOutputAlignmentCollector();
        p.addStdErrConsumer(l -> LOGGER.info("External (ERR): {}",l));
        p.addStdOutConsumer(l -> LOGGER.info("External (OUT): {}",l));
        if(isUsingStdOut()){
            p.addStdOutConsumer(alignmentCollector);
        }else{
            p.addStdErrConsumer(alignmentCollector);
        }
        p.run();
        URL detectedURL = alignmentCollector.getURL(); 
        if(detectedURL == null){
            LOGGER.warn("Did not find an URL in the output of the external process. Return input alignment");
            return inputAlignment;
        }
        return detectedURL;
    }
    
    
    
    
    
    
}
