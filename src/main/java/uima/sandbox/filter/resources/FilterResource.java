package uima.sandbox.filter.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.apache.uima.resource.SharedResourceObject;

public interface FilterResource extends SharedResourceObject {

	public static final String KEY_FILTERS = "Filter";

	public Set<String> getFilters();

	public void load(InputStream inputStream) throws IOException;
	
	public void store(OutputStream outputStream) throws IOException;

}
