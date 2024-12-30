/*
 * Copyright 2013-2015 must-be.org
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

package consulo.microsoft.dotnet.sdk;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.PredefinedBundlesProvider;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.dotnet.sdk.DotNetVersion;
import consulo.platform.Platform;

import jakarta.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 09.03.2015
 */
@ExtensionImpl
public class MicrosoftDotNetPredefinedBundlesProvider extends PredefinedBundlesProvider
{
	@Override
	public void createBundles(@Nonnull Context context)
	{
		MicrosoftDotNetSdkType sdkType = MicrosoftDotNetSdkType.getInstance();

		Collection<MicrosoftDotNetFramework> microsoftDotNetFrameworks = buildPaths(sdkType);
		for(MicrosoftDotNetFramework netFramework : microsoftDotNetFrameworks)
		{
			Sdk sdk = context.createSdkWithName(sdkType, sdkType.getPresentableName() + " " + netFramework.toString());

			SdkModificator modificator = sdk.getSdkModificator();
			modificator.setHomePath(netFramework.getPath());
			modificator.setVersionString(netFramework.getVersion().getPresentableName());
			modificator.commitChanges();

			sdkType.setupSdkPaths(sdk);
		}
	}

	public Collection<MicrosoftDotNetFramework> buildPaths(MicrosoftDotNetSdkType sdkType)
	{
		if(Platform.current().os().isWindows())
		{
			List<MicrosoftDotNetFramework> list = new ArrayList<>();

			// first of all, we try to collect sdk from Windows dir, where compilers are located at same dir
			File framework = new File(Platform.current().os().getEnvironmentVariable("windir"), "Microsoft.NET/Framework");
			File[] files = framework.listFiles();
			if(files != null)
			{
				for(File file : files)
				{
					DotNetVersion version = DotNetVersion.findVersion(file.getName(), true);
					if(version != null && sdkType.isValidSdkHome(file.getPath()))
					{
						list.add(new MicrosoftDotNetFramework(version, file.getPath(), true));
					}
				}
			}

			collectFromReferenceAssemblies(list, sdkType, "ProgramFiles");
			collectFromReferenceAssemblies(list, sdkType, "ProgramFiles(x86)");
			return list;
		}
		return Collections.emptyList();
	}


	private void collectFromReferenceAssemblies(Collection<MicrosoftDotNetFramework> set,
			@Nonnull MicrosoftDotNetSdkType sdkType,
			@Nonnull String env)
	{
		String envValue = Platform.current().os().getEnvironmentVariable(env);
		if(envValue == null)
		{
			return;
		}

		File path = new File(envValue, "Reference Assemblies\\Microsoft\\Framework\\.NETFramework");
		File[] files = path.listFiles();
		if(files == null)
		{
			return;
		}

		for(File file : files)
		{
			DotNetVersion version = DotNetVersion.findVersion(file.getName(), false);
			if(version != null && sdkType.isValidSdkHome(file.getPath()))
			{
				set.add(new MicrosoftDotNetFramework(version, file.getPath(), false));
			}
		}
	}
}
