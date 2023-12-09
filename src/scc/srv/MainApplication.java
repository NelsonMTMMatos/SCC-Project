package scc.srv;

import jakarta.ws.rs.core.Application;
import scc.srv.resource.*;

import java.util.HashSet;
import java.util.Set;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		singletons.add( new HouseResource());
		singletons.add( new MediaResource());
		singletons.add( new PeriodResource());
		singletons.add( new QuestionResource());
		singletons.add( new RentalResource());
		singletons.add( new UserResource());

		resources.add(ControlResource.class);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
