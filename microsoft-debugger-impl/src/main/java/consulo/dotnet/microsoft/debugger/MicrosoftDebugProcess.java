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

package consulo.dotnet.microsoft.debugger;

import consulo.dotnet.debugger.impl.DotNetDebugProcessBase;
import consulo.dotnet.debugger.impl.DotNetSuspendContext;
import consulo.dotnet.debugger.impl.breakpoint.DotNetExceptionBreakpointType;
import consulo.dotnet.debugger.impl.breakpoint.DotNetLineBreakpointType;
import consulo.dotnet.debugger.impl.breakpoint.properties.DotNetExceptionBreakpointProperties;
import consulo.dotnet.microsoft.debugger.breakpoint.MicrosoftBreakpointUtil;
import consulo.dotnet.util.DebugConnectionInfo;
import consulo.execution.configuration.RunProfile;
import consulo.execution.debug.*;
import consulo.execution.debug.breakpoint.XBreakpoint;
import consulo.execution.debug.breakpoint.XBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.execution.debug.event.XBreakpointListener;
import consulo.execution.debug.frame.XSuspendContext;
import mssdw.DebugInformationResult;
import mssdw.ThreadMirror;
import mssdw.event.EventSet;
import mssdw.request.BreakpointRequest;
import mssdw.request.EventRequestManager;
import mssdw.request.StepRequest;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 5/8/2016
 */
public class MicrosoftDebugProcess extends DotNetDebugProcessBase
{
	private class MyXBreakpointListener implements XBreakpointListener<XBreakpoint<?>>
	{
		@Override
		public void breakpointAdded(@Nonnull final XBreakpoint<?> breakpoint)
		{
			myDebugThread.invoke(virtualMachine ->
			{
				XBreakpointType<?, ?> type = breakpoint.getType();
				if(type == DotNetLineBreakpointType.getInstance())
				{
					MicrosoftBreakpointUtil.createBreakpointRequest(getSession(), virtualMachine, (XLineBreakpoint) breakpoint);
				}
				else if(type == DotNetExceptionBreakpointType.getInstance())
				{
					MicrosoftBreakpointUtil.createExceptionRequest(virtualMachine, (XBreakpoint<DotNetExceptionBreakpointProperties>) breakpoint);
				}
			});
		}

		@Override
		public void breakpointRemoved(@Nonnull final XBreakpoint<?> breakpoint)
		{
			myDebugThread.invoke(virtualMachine -> virtualMachine.stopBreakpointRequests(breakpoint));
		}

		@Override
		public void breakpointChanged(@Nonnull XBreakpoint<?> breakpoint)
		{
			if(breakpoint.isEnabled())
			{
				breakpointAdded(breakpoint);
			}
			else
			{
				breakpointRemoved(breakpoint);
			}
		}
	}

	private final DebugConnectionInfo myDebugConnectionInfo;
	private final MicrosoftDebugThread myDebugThread;

	private EventSet myPausedEventSet;
	private XBreakpointManager myBreakpointManager;
	private final XBreakpointListener<XBreakpoint<?>> myBreakpointListener = new MyXBreakpointListener();

	public MicrosoftDebugProcess(XDebugSession session, RunProfile runProfile, DebugConnectionInfo debugConnectionInfo)
	{
		super(session, runProfile);
		session.setPauseActionSupported(true);
		myDebugConnectionInfo = debugConnectionInfo;
		myDebugThread = new MicrosoftDebugThread(session, this, myDebugConnectionInfo);

		myBreakpointManager = XDebuggerManager.getInstance(session.getProject()).getBreakpointManager();
		myBreakpointManager.addBreakpointListener(myBreakpointListener);
	}

	@Nonnull
	public MicrosoftDebugThread getDebugThread()
	{
		return myDebugThread;
	}

	@Override
	public void start()
	{
		myDebugThread.start();
	}

	@Override
	public void startPausing()
	{
		myDebugThread.addCommand(virtualMachine ->
		{
			virtualMachine.suspend();
			getSession().positionReached(new DotNetSuspendContext(createDebugContext(virtualMachine, null), -1));
			return false;
		});
	}

	@Override
	public void runToPosition(@Nonnull final XSourcePosition position, @Nullable XSuspendContext context)
	{
		if(myPausedEventSet == null)
		{
			return;
		}

		myDebugThread.addCommand(virtualMachine ->
		{
			virtualMachine.stopStepRequests();

			DebugInformationResult debugOffset = virtualMachine.getDelegate().findDebugOffset(position.getFile().getPath(), position.getLine() + 1, -1);
			if(debugOffset != null)
			{
				EventRequestManager eventRequestManager = virtualMachine.eventRequestManager();

				BreakpointRequest breakpointRequest = eventRequestManager.createBreakpointRequest(debugOffset);
				breakpointRequest.putProperty(RUN_TO_CURSOR, Boolean.TRUE);
				breakpointRequest.enable();
			}
			return true;
		});
	}

	@Override
	public void resume(@Nullable XSuspendContext context)
	{
		myPausedEventSet = null;
		myDebugThread.addCommand(virtualMachine ->
		{
			virtualMachine.stopStepRequests();

			return true;
		});
	}

	@Override
	public String getCurrentStateMessage()
	{
		if(myDebugThread.isConnected())
		{
			return "Connected to " + myDebugConnectionInfo.getHost() + ":" + myDebugConnectionInfo.getPort();
		}
		return XDebuggerBundle.message("debugger.state.message.disconnected");
	}

	@Override
	public void startStepOver(@Nullable XSuspendContext context)
	{
		stepRequest(StepRequest.StepDepth.Over);
	}

	@Override
	public void startStepInto(@Nullable XSuspendContext context)
	{
		stepRequest(StepRequest.StepDepth.Into);
	}

	@Override
	public void startStepOut(@Nullable XSuspendContext context)
	{
		stepRequest(StepRequest.StepDepth.Out);
	}

	private void stepRequest(final StepRequest.StepDepth stepDepth)
	{
		if(myPausedEventSet == null)
		{
			return;
		}
		final ThreadMirror threadMirror = myPausedEventSet.eventThread();
		if(threadMirror == null)
		{
			return;
		}

		myDebugThread.addCommand(virtualMachine ->
		{
			EventRequestManager eventRequestManager = virtualMachine.eventRequestManager();
			StepRequest stepRequest = eventRequestManager.createStepRequest(threadMirror, stepDepth);
			stepRequest.enable();

			virtualMachine.addStepRequest(stepRequest);
			return true;
		});
	}

	@Override
	public void stopImpl()
	{
		myPausedEventSet = null;
		myDebugThread.setStop();
		normalizeBreakpoints();
		myBreakpointManager.removeBreakpointListener(myBreakpointListener);
	}

	public void setPausedEventSet(EventSet pausedEventSet)
	{
		myPausedEventSet = pausedEventSet;
	}
}
