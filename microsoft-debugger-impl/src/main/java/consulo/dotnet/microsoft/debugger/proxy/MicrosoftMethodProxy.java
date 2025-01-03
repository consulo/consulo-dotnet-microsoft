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

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.debugger.proxy.*;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import mssdw.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 5/8/2016
 */
public class MicrosoftMethodProxy implements DotNetMethodProxy
{
	private MethodMirror myMethodMirror;

	public MicrosoftMethodProxy(MethodMirror methodMirror)
	{
		myMethodMirror = methodMirror;
	}

	@Override
	public boolean isStatic()
	{
		return myMethodMirror.isStatic();
	}

	@Override
	public boolean isAbstract()
	{
		return myMethodMirror.isAbstract();
	}

	@Override
	public boolean isAnnotatedBy(@Nonnull String attributeVmQName)
	{
		return ArrayUtil.contains(attributeVmQName, myMethodMirror.customAttributes());
	}

	@Nonnull
	@Override
	public DotNetTypeProxy getDeclarationType()
	{
		return MicrosoftTypeProxy.of(myMethodMirror.declaringType());
	}

	@Nonnull
	@Override
	public DotNetMethodParameterProxy[] getParameters()
	{
		MethodParameterMirror[] parameters = myMethodMirror.parameters();
		DotNetMethodParameterProxy[] proxies = new DotNetMethodParameterProxy[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			MethodParameterMirror parameter = parameters[i];
			proxies[i] = new MicrosoftMethodParameterProxy(parameter);
		}
		return proxies;
	}

	@Nonnull
	@Override
	public DotNetLocalVariableProxy[] getLocalVariables(@Nonnull DotNetStackFrameProxy frameProxy)
	{
		MicrosoftStackFrameProxy proxy = (MicrosoftStackFrameProxy) frameProxy;

		LocalVariableMirror[] locals = myMethodMirror.locals(proxy.getFrameMirror());
		DotNetLocalVariableProxy[] proxies = new DotNetLocalVariableProxy[locals.length];
		for(int i = 0; i < locals.length; i++)
		{
			LocalVariableMirror local = locals[i];
			proxies[i] = new MicrosoftLocalVariableProxy(local);
		}
		return proxies;
	}

	@Nullable
	@Override
	public DotNetValueProxy invoke(@Nonnull DotNetStackFrameProxy frameProxy, @Nullable DotNetValueProxy thisObjectProxy, @Nonnull DotNetValueProxy... arguments) throws DotNetThrowValueException
	{
		StackFrameMirror frameMirror = ((MicrosoftStackFrameProxy) frameProxy).getFrameMirror();
		Value<?> thisObject = thisObjectProxy == null ? null : ((MicrosoftValueProxyBase) thisObjectProxy).getMirror();

		Value[] values = new Value[arguments.length];
		for(int i = 0; i < arguments.length; i++)
		{
			DotNetValueProxy argument = arguments[i];
			values[i] = ((MicrosoftValueProxyBase) argument).getMirror();
		}
		try
		{
			return MicrosoftValueProxyUtil.wrap(myMethodMirror.invoke(frameMirror, thisObject, values));
		}
		catch(ThrowValueException e)
		{
			throw new IllegalArgumentException(e); //TODO [VISTALL] throw new DotNetThrowValueException(MonoValueProxyUtil.wrap(e.getThrowExceptionValue()));
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement findExecutableElementFromDebugInfo(@Nonnull Project project, int executableChildrenAtLineIndex)
	{
		return null;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return myMethodMirror.name();
	}
}
