/*
 * Copyright 2013 must-be.org
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

package consulo.microsoft.dotnet.module.extension;

import consulo.container.plugin.PluginManager;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.compiler.DotNetMacroUtil;
import consulo.dotnet.debugger.impl.DotNetDebugProcessBase;
import consulo.dotnet.debugger.impl.DotNetModuleExtensionWithDebug;
import consulo.dotnet.microsoft.debugger.MicrosoftDebugProcess;
import consulo.dotnet.module.extension.BaseDotNetModuleExtension;
import consulo.dotnet.util.DebugConnectionInfo;
import consulo.execution.configuration.RunProfile;
import consulo.execution.debug.XDebugSession;
import consulo.microsoft.dotnet.sdk.MicrosoftDotNetSdkType;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;

/**
 * @author VISTALL
 * @since 20.11.13.
 */
public class MicrosoftDotNetModuleExtension extends BaseDotNetModuleExtension<MicrosoftDotNetModuleExtension> implements DotNetModuleExtensionWithDebug
{
	public MicrosoftDotNetModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module)
	{
		super(id, module);
	}

	@Nonnull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return MicrosoftDotNetSdkType.class;
	}

	@Nonnull
	@Override
	public GeneralCommandLine createDefaultCommandLine(@Nonnull Sdk sdk, @Nullable DebugConnectionInfo debugConnectionInfo) throws ExecutionException
	{
		String fileName = DotNetMacroUtil.expandOutputFile(this);

		return createRunCommandLineImpl(fileName, debugConnectionInfo, sdk);
	}

	@Nonnull
	@Override
	public String getDebugFileExtension()
	{
		return "pdb";
	}

	@Nonnull
	public static GeneralCommandLine createRunCommandLineImpl(@Nonnull String fileName, @Nullable DebugConnectionInfo debugConnectionInfo, @Nonnull Sdk sdk)
	{
		GeneralCommandLine commandLine = new GeneralCommandLine();
		if(debugConnectionInfo != null)
		{
			String mssdwPath = Platform.current().os().getEnvironmentVariable("mssdw.path");
			if(mssdwPath != null)
			{
				commandLine.withExePath(mssdwPath);
			}
			else
			{
				File pluginPath = PluginManager.getPluginPath(MicrosoftDotNetModuleExtension.class);
				commandLine.setExePath(new File(pluginPath, "mssdw\\mssdw.exe").getPath());
			}
			commandLine.addParameter("--port=" + debugConnectionInfo.getPort());
			commandLine.addParameter(fileName);
		}
		else
		{
			commandLine.setExePath(fileName);
		}
		return commandLine;
	}

	@Nonnull
	@Override
	public DotNetDebugProcessBase createDebuggerProcess(@Nonnull XDebugSession session, @Nonnull RunProfile runProfile, @Nonnull DebugConnectionInfo debugConnectionInfo)
	{
		return new MicrosoftDebugProcess(session, runProfile, debugConnectionInfo);
	}
}
