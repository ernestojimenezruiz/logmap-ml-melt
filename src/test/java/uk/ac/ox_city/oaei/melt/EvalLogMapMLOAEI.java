package uk.ac.ox_city.oaei.melt;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorBasic;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCopyResults;
import uk.ac.ox_city.logmap_ml.melt.LogMapML_MELT;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EvalLogMapMLOAEI {
    
	private static final Logger LOGGER = LoggerFactory.getLogger(EvalLogMapMLOAEI.class);
	
	private static enum System {MatcherURL, MactherOWLAPI, SimpleJena, NODocker};
		
    public static void main(String[] args) throws Exception {
    
    	    	
    	File dockerFile;
    	MatcherDockerFile dockerMatcher;
    	ExecutionResultSet ers;
    	
    	
    	System system;
    	system = System.MactherOWLAPI;
    	//system = System.MatcherURL;
    	//system = System.SimpleJena;
    	
    	if (system == System.NODocker) { //Local MELT system or built-in MELT matchers
    		
    		LogMapML_MELT matcher = new LogMapML_MELT();
            //ers = Executor.run(TrackRepository.Conference.V1.getFirstTestCase(), matcher);
            ers = Executor.run(TrackRepository.Anatomy.Default.getTestCases(), matcher);
            
    	}
    	else {
	    	if (system == System.MactherOWLAPI) {
		        dockerFile = new File("/home/ernesto/Documents/OAEI2021/evaluation/logmap-melt-owlapi-oaei-2021-web-latest.tar.gz");
		        dockerMatcher = new MatcherDockerFile("logmap-melt-owlapi-oaei-2021-web:latest", dockerFile);//
	    	}
	    	else if (system == System.MatcherURL) {
		        dockerFile = new File("/home/ernesto/Documents/OAEI2021/evaluation/logmap-melt-oaei-2021-web-latest.tar.gz");
		        dockerMatcher = new MatcherDockerFile("logmap-melt-oaei-2021-web:latest", dockerFile);//
	    	}
	    	else {// if (system == System.SimpleJena) {
	    		dockerFile = new File("/home/ernesto/Documents/OAEI2021/evaluation/simplewebmatcher-1.0-web-latest.tar.gz");
	    		dockerMatcher = new MatcherDockerFile("simplewebmatcher-1.0-web:latest", dockerFile);//
	    	}
	    	
	    	//LOGGER.info(MatcherDockerFile.getImageNameFromFile(dockerFile));
	        dockerMatcher.logAllLinesFromContainer();
	        
	        // running the matcher on any task
	        //ers = Executor.run(TrackRepository.Anatomy.Default.getFirstTestCase(), dockerMatcher);
	        ers = Executor.run(TrackRepository.Conference.V1.getTestCases(), dockerMatcher);
	        
	        
	        //TODO
	        // we should close the docker matcher so that docker cab shut down the container
	        //Leave it open for debugging purposes to access the logs: "docker container logs {id_container}"
	        //dockerMatcher.close();
	    
    	}
       
        
        // evaluating our system
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(ers);
        EvaluatorBasic evaluatorBasic = new EvaluatorBasic(ers);        
        EvaluatorCopyResults evalCopy = new EvaluatorCopyResults(ers);

        // writing evaluation results to disk
        evaluatorCSV.writeToDirectory("/home/ernesto/Documents/OAEI2021/evaluation/results/");
        evaluatorBasic.writeToDirectory("/home/ernesto/Documents/OAEI2021/evaluation/results/");
        evalCopy.writeToDirectory("/home/ernesto/Documents/OAEI2021/evaluation/results/");
        //evalCopy.writeResultsToDirectory(new File("/home/ernesto/Documents/OAEI2021/evaluation/results/"));

    }
}
