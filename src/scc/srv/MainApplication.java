package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.serverless.HttpFunction;
import scc.srv.resource.*;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);

		singletons.add( new HouseResource());
		singletons.add( new MediaResource());
		singletons.add( new PeriodResource());
		singletons.add( new QuestionResource());
		singletons.add( new RentalResource());
		singletons.add( new UserResource());
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
