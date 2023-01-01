/*
 * Copyright 2013-2016 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.dotnet.microsoft.debugger.proxy;

import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.dotnet.debugger.proxy.DotNetThreadProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.DotNetVirtualMachineProxy;
import consulo.dotnet.debugger.proxy.value.*;
import consulo.execution.debug.breakpoint.XBreakpoint;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.MultiMap;
import consulo.virtualFileSystem.VirtualFile;
import mssdw.*;
import mssdw.request.EventRequest;
import mssdw.request.EventRequestManager;
import mssdw.request.StepRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author VISTALL
 * @since 5/8/2016
 */
public class MicrosoftVirtualMachineProxy implements DotNetVirtualMachineProxy
{
	private static final Logger LOG = Logger.getInstance(MicrosoftVirtualMachineProxy.class);

	private final Set<StepRequest> myStepRequests = new LinkedHashSet<>();
	private final MultiMap<XBreakpoint, EventRequest> myBreakpointEventRequests = MultiMap.create();
	private final List<String> myLoadedModules = new CopyOnWriteArrayList<String>();
	private final VirtualMachine myVirtualMachine;

	private final ExecutorService myExecutor = AppExecutorUtil.createBoundedApplicationPoolExecutor("MicrosoftNET Debugger", 1);

	public MicrosoftVirtualMachineProxy(@Nonnull VirtualMachine virtualMachine)
	{
		myVirtualMachine = virtualMachine;
	}

	@Nullable
	@Override
	public DotNetTypeProxy findType(@Nonnull Project project, @Nonnull String vmQName, @Nonnull VirtualFile virtualFile)
	{
		return MicrosoftTypeProxy.of(myVirtualMachine.findTypeByQualifiedName(vmQName));
	}

	@Nullable
	@Override
	public DotNetTypeProxy findTypeInCorlib(@Nonnull String vmQName)
	{
		TypeMirror typeMirror = myVirtualMachine.findTypeByQualifiedName(vmQName);
		return MicrosoftTypeProxy.of(typeMirror);
	}

	@Override
	public void invoke(@Nonnull Runnable runnable)
	{
		myExecutor.execute(() ->
		{
			try
			{
				runnable.run();
			}
			catch(Throwable e)
			{
				LOG.error(e);
			}
		});
	}

	@Nonnull
	@Override
	public List<DotNetThreadProxy> getThreads()
	{
		return ContainerUtil.map(myVirtualMachine.allThreads(), threadMirror ->
		{
			return new MicrosoftThreadProxy(MicrosoftVirtualMachineProxy.this, threadMirror);
		});
	}

	@Nonnull
	@Override
	public DotNetStringValueProxy createStringValue(@Nonnull String value)
	{
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public DotNetCharValueProxy createCharValue(char value)
	{
		return MicrosoftValueProxyUtil.wrap(new CharValueMirror(myVirtualMachine, value));
	}

	@Nonnull
	@Override
	public DotNetBooleanValueProxy createBooleanValue(boolean value)
	{
		return MicrosoftValueProxyUtil.wrap(new BooleanValueMirror(myVirtualMachine, value));
	}

	@Nonnull
	@Override
	public DotNetNumberValueProxy createNumberValue(int tag, @Nonnull Number value)
	{
		return MicrosoftValueProxyUtil.wrap(new NumberValueMirror(myVirtualMachine, tag, value));
	}

	@Nonnull
	@Override
	public DotNetNullValueProxy createNullValue()
	{
		return MicrosoftValueProxyUtil.wrap(new NoObjectValueMirror(myVirtualMachine));
	}

	public void dispose()
	{
		myExecutor.shutdown();
		myStepRequests.clear();
		myVirtualMachine.dispose();
		myBreakpointEventRequests.clear();
	}

	public void addStepRequest(@Nonnull StepRequest stepRequest)
	{
		myStepRequests.add(stepRequest);
	}

	public void stopStepRequest(@Nonnull StepRequest stepRequest)
	{
		stepRequest.disable();
		myStepRequests.remove(stepRequest);
	}

	public void putRequest(@Nonnull XBreakpoint<?> breakpoint, @Nonnull EventRequest request)
	{
		myBreakpointEventRequests.putValue(breakpoint, request);
	}

	@Nullable
	public XBreakpoint<?> findBreakpointByRequest(@Nonnull EventRequest eventRequest)
	{
		for(Map.Entry<XBreakpoint, Collection<EventRequest>> entry : myBreakpointEventRequests.entrySet())
		{
			if(entry.getValue().contains(eventRequest))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	public void stopBreakpointRequests(XBreakpoint<?> breakpoint)
	{
		Collection<EventRequest> eventRequests = myBreakpointEventRequests.remove(breakpoint);
		if(eventRequests == null)
		{
			return;
		}
		for(EventRequest eventRequest : eventRequests)
		{
			eventRequest.disable();
		}
		myVirtualMachine.eventRequestManager().deleteEventRequests(eventRequests);
	}

	public void stopStepRequests()
	{
		for(StepRequest stepRequest : myStepRequests)
		{
			stepRequest.disable();
		}
		myStepRequests.clear();
	}

	public EventRequestManager eventRequestManager()
	{
		return myVirtualMachine.eventRequestManager();
	}

	@Nonnull
	public VirtualMachine getDelegate()
	{
		return myVirtualMachine;
	}

	@Nonnull
	private static String getAssemblyName(String name)
	{
		int i = name.indexOf(',');
		if(i == -1)
		{
			return name;
		}
		return name.substring(0, i);
	}

	public void resume()
	{
		try
		{
			myVirtualMachine.resume();
		}
		catch(NotSuspendedException ignored)
		{
		}
	}

	public void suspend()
	{
		myVirtualMachine.suspend();
	}

	@Nonnull
	public List<ThreadMirror> allThreads()
	{
		return myVirtualMachine.allThreads();
	}

	public void addLoadedModule(String path)
	{
		myLoadedModules.add(path);
	}

	public List<String> getLoadedModules()
	{
		return myLoadedModules;
	}
}
