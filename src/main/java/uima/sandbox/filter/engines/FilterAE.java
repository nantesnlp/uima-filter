package uima.sandbox.filter.engines;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import uima.sandbox.filter.resources.FilterResource;

public class FilterAE extends JCasAnnotator_ImplBase {
	
	//resources
	@ExternalResource(key=FilterResource.KEY_FILTERS)
	private FilterResource filter;

	// parameters
	public static final String PARAM_FEATURE = "Feature";
	@ConfigurationParameter(name = PARAM_FEATURE, mandatory=true)
	private String feature;

	private boolean filterIn = false;
	
	private boolean filterOut = true;

//	private String path;
	private Type type;
	private Feature feat;
	
	
//	private void setPath(String path) throws ResourceInitializationException {
//		try {
//			this.path = path;
//			InputStream stream = new FileInputStream(path);
//			this.filter.load(stream);
//		} catch (IOException e ) {
//			throw new ResourceInitializationException(e);
//		}
//	}
//	
	private void setFeature(JCas cas) throws AnalysisEngineProcessException {
		
		if (this.type == null || this.feat == null) {
			try {
				String[] elements = this.feature.split(":");
				if (elements.length == 2) {
					this.type = cas.getRequiredType(elements[0].trim());
					String feat = elements[1];
					if (feat.equals("coveredText")) {
						this.feat = null;
					}
				} else {
					this.type = cas.getRequiredType(this.feature);
					this.feat = null;
				}
			} catch (CASException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}
		

	
	private void enableFilterIn(boolean filterIn,boolean filterOut) throws Exception {
		if (filterIn && !filterOut) {
			this.filterIn = false;
		} else if (!filterIn && filterOut) {
			this.filterIn = true;
		} else {
			throw new Exception("Incompatible parameter setting: 'Filter In' boolean value should be different as that of 'Filter Out'");
		}
	}
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			this.filter = (FilterResource) context.getResourceObject("Filter");
			
			this.feature = (String) context.getConfigParameterValue(PARAM_FEATURE);
			this.enableFilterIn(filterIn,filterOut);
//			this.path = (String) context.getConfigParameterValue("File");
//			if (this.path != null && this.path == null) {
//				this.setPath(path);
//			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
	}

	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		this.setFeature(cas);
		List<Annotation> annotations = new ArrayList<Annotation>();
		AnnotationIndex<Annotation> index = cas.getAnnotationIndex(this.type);
		FSIterator<Annotation> iter = index.iterator();
		while(iter.hasNext()) {
			Annotation annotation = iter.next();
			String value = null;
			if (this.feat == null) { 
				value = annotation.getCoveredText();
			} else {	
				value = annotation.getStringValue(this.feat);
			}
			if (value == null) {
				continue;
			} else {
				if (this.filter.getFilters().contains(value)) {
					if (this.filterIn) {
						annotations.add(annotation);
					}
				} else {
					if (!this.filterOut) {
						annotations.add(annotation);
					}
				}
			}
		}
		for (Annotation annotation : annotations) {
			annotation.removeFromIndexes();
		}
		}
	
}
