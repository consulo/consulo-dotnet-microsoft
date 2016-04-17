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

import org.jetbrains.annotations.Nullable;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import consulo.dotnet.microsoft.debugger.protocol.clientMessage.GetFramesRequest;
import consulo.dotnet.microsoft.debugger.protocol.serverMessage.GetFramesRequestResult;

/**
 * @author VISTALL
 * @since 18.04.2016
 */
public class MicrosoftExecutionStack extends XExecutionStack
{
	private int myThreadId;

	public MicrosoftExecutionStack(MicrosoftDebuggerClientContext context, String displayName, int threadId)
	{
		super(displayName);
		myThreadId = threadId;

		GetFramesRequestResult o = context.sendAndReceive(new GetFramesRequest(threadId));
	}

	@Nullable
	@Override
	public XStackFrame getTopFrame()
	{
		return null;
	}

	@Override
	public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container)
	{

	}

	public int getThreadId()
	{
		return myThreadId;
	}
}
