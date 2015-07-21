package uima.sandbox.filter.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;

import uima.sandbox.filter.models.Filter;
import uima.sandbox.filter.models.FilterFactory;
import uima.sandbox.filter.models.Item;

public class DefaultFilterResource implements FilterResource {

	private Set<String> filter;
	
	private void compile() {
		this.filter = new HashSet<String>();
		if (this.model != null) {
			for (Item item : this.model.getItem()) {
				String value = item.getValue();
				this.filter.add(value);
			}
		}
	}
	
	@Override
	public Set<String> getFilters() {
		return this.filter;
	}

	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		try {
			this.load(data.getInputStream());
		} catch (Exception e) {
			throw new  ResourceInitializationException(e);
		} 
	}
	
	private Filter model;
	
	@Override
	public void load(InputStream inputStream) throws IOException {
		if (this.model == null) {
			try {
				JAXBContext context = JAXBContext.newInstance(Filter.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				StreamSource source = new StreamSource(inputStream);
				JAXBElement<Filter> root = unmarshaller.unmarshal(source, Filter.class);
				this.model = root.getValue();
				this.compile();
			} catch (JAXBException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public void store(OutputStream outputStream) throws IOException {
		if (this.model != null) {
			try {
				FilterFactory factory = new FilterFactory();
				JAXBContext context = JAXBContext.newInstance(Filter.class);
				JAXBElement<Filter> element = factory.createFilter(this.model);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
				marshaller.marshal(element, outputStream);
			} catch (JAXBException e) {
				throw new IOException(e);
			}
		}
	}

}
