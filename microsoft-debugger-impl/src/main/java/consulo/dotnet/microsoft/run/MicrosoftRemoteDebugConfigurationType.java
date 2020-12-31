package consulo.dotnet.microsoft.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import consulo.dotnet.debugger.DotNetDebugProcessBase;
import consulo.dotnet.execution.DebugConnectionInfo;
import consulo.dotnet.microsoft.debugger.MicrosoftDebugProcess;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.run.remote.DotNetRemoteConfiguration;
import consulo.module.extension.ModuleExtensionHelper;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 31/12/2020
 */
public class MicrosoftRemoteDebugConfigurationType extends ConfigurationTypeBase
{
	public MicrosoftRemoteDebugConfigurationType()
	{
		super("MicrosoftRemoteDebugConfigurationType", ".NET Remote", "", AllIcons.RunConfigurations.Remote);

		addFactory(new ConfigurationFactoryEx(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new DotNetRemoteConfiguration(project, this)
				{
					@Nonnull
					@Override
					public DotNetDebugProcessBase createDebuggerProcess(@Nonnull XDebugSession session, @Nonnull DebugConnectionInfo info) throws ExecutionException
					{
						MicrosoftDebugProcess process = new MicrosoftDebugProcess(session, this, info);
						//						process.getDebugThread().addListener(new MonoVirtualMachineListener()
						//						{
						//							@Override
						//							public void connectionSuccess(@Nonnull VirtualMachine machine)
						//							{
						//								ProcessHandler processHandler = process.getProcessHandler();
						//								processHandler.notifyTextAvailable(String.format("Success attach to %s:%d", info.getHost(), info.getPort()), ProcessOutputTypes.STDOUT);
						//							}
						//
						//							@Override
						//							public void connectionStopped()
						//							{
						//							}
						//
						//							@Override
						//							public void connectionFailed()
						//							{
						//								ProcessHandler processHandler = process.getProcessHandler();
						//								processHandler.notifyTextAvailable(String.format("Failed attach to %s:%d", info.getHost(), info.getPort()), ProcessOutputTypes.STDERR);
						//								StopProcessAction.stopProcess(processHandler);
						//							}
						//						});
						return process;
					}
				};
			}

			@Override
			public boolean isApplicable(@Nonnull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(DotNetModuleExtension.class);
			}
		});
	}
}
