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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.dotnet.debugger.proxy.DotNetAbsentInformationException;
import consulo.dotnet.debugger.proxy.DotNetInvalidObjectException;
import consulo.dotnet.debugger.proxy.DotNetInvalidStackFrameException;
import consulo.dotnet.debugger.proxy.DotNetLocalVariableProxy;
import consulo.dotnet.debugger.proxy.DotNetMethodParameterProxy;
import consulo.dotnet.debugger.proxy.DotNetSourceLocation;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetThreadProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import mssdw.AbsentInformationException;
import mssdw.InvalidObjectException;
import mssdw.InvalidStackFrameException;
import mssdw.StackFrameMirror;

/**
 * @author VISTALL
 * @since 5/8/2016
 */
public class MicrosoftStackFrameProxy implements DotNetStackFrameProxy
{
	private final int myIndex;
	private final MicrosoftVirtualMachineProxy myVirtualMachineProxy;
	private final StackFrameMirror myFrameMirror;

	private final int myMethodId;

	public MicrosoftStackFrameProxy(int index, MicrosoftVirtualMachineProxy virtualMachineProxy, StackFrameMirror frameMirror)
	{
		myIndex = index;
		myVirtualMachineProxy = virtualMachineProxy;
		myFrameMirror = frameMirror;

		myMethodId = frameMirror.getFunctionId();
	}

	@Nonnull
	public StackFrameMirror getFrameMirror()
	{
		return myFrameMirror;
	}

	@Nonnull
	@Override
	public DotNetValueProxy getThisObject() throws DotNetInvalidObjectException, DotNetInvalidStackFrameException, DotNetAbsentInformationException
	{
		try
		{
			return MicrosoftValueProxyUtil.wrap(myFrameMirror.thisObject());
		}
		catch(AbsentInformationException e)
		{
			throw new DotNetAbsentInformationException(e);
		}
		catch(InvalidObjectException e)
		{
			throw new DotNetInvalidObjectException(e);
		}
		catch(InvalidStackFrameException e)
		{
			throw new DotNetInvalidStackFrameException(e);
		}
	}

	@Nullable
	@Override
	public DotNetValueProxy getParameterValue(@Nonnull DotNetMethodParameterProxy parameterProxy)
	{
		MicrosoftMethodParameterProxy proxy = (MicrosoftMethodParameterProxy) parameterProxy;
		return MicrosoftValueProxyUtil.wrap(myFrameMirror.argumentValue(proxy.getMirror()));
	}

	@Override
	public void setParameterValue(@Nonnull DotNetMethodParameterProxy parameterProxy, @Nonnull DotNetValueProxy valueProxy)
	{
	}

	@Nullable
	@Override
	public DotNetValueProxy getLocalValue(@Nonnull DotNetLocalVariableProxy localVariableProxy)
	{
		MicrosoftLocalVariableProxy proxy = (MicrosoftLocalVariableProxy) localVariableProxy;
		return MicrosoftValueProxyUtil.wrap(myFrameMirror.localValue(proxy.getMirror()));
	}

	@Override
	public void setLocalValue(@Nonnull DotNetLocalVariableProxy localVariableProxy, @Nonnull DotNetValueProxy valueProxy)
	{
	}

	@Nonnull
	@Override
	public DotNetThreadProxy getThread()
	{
		return new MicrosoftThreadProxy(myVirtualMachineProxy, myFrameMirror.thread());
	}

	@Override
	public int getIndex()
	{
		return myIndex;
	}

	@Nonnull
	@Override
	public Object getEqualityObject()
	{
		return myMethodId;
	}

	@Nullable
	@Override
	public DotNetSourceLocation getSourceLocation()
	{
		return new MicrosoftSourceLocation(myFrameMirror);
	}
}
