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

package consulo.microsoft.dotnet.sdk;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.microsoft.icon.MicrosoftDotNetIconGroup;
import consulo.dotnet.sdk.DotNetSdkType;
import consulo.localize.LocalizeValue;
import consulo.microsoft.dotnet.util.MicrosoftDotNetUtil;
import consulo.platform.Platform;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 23.11.13.
 */
@ExtensionImpl
public class MicrosoftDotNetSdkType extends DotNetSdkType {
    @Nonnull
    public static MicrosoftDotNetSdkType getInstance() {
        return Application.get().getExtensionPoint(SdkType.class).findExtensionOrFail(MicrosoftDotNetSdkType.class);
    }

    public MicrosoftDotNetSdkType() {
        super("MICROSOFT_DOTNET_SDK", LocalizeValue.localizeTODO(".NET Framework"), MicrosoftDotNetIconGroup.dotnet());
    }

    @Override
    public boolean supportsUserAdd() {
        return false;
    }

    @Override
    public void collectHomePaths(@Nonnull Platform platform, @Nonnull Consumer<Path> pathConsumer) {
        // the bundles are predefined - see MicrosoftDotNetPredefinedBundlesProvider
    }

    @Override
    public boolean isValidSdkHome(@Nonnull Platform platform, @Nonnull Path path) {
        return Files.exists(path.resolve("mscorlib.dll"));
    }

    @Nullable
    @Override
    public String getVersionString(@Nonnull Platform platform, @Nonnull Path sdkHome) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String suggestSdkName(@Nonnull Platform platform, @Nullable String currentSdkName, @Nonnull Path sdkHome) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Path getLoaderFile(@Nonnull Sdk sdk) {
        String targetVersion = MicrosoftDotNetUtil.getTargetVersion();
        if ("4.0.0".equals(targetVersion)) {
            return getLoaderFile(MicrosoftDotNetSdkType.class, "loader4.exe");
        }
        return super.getLoaderFile(sdk);
    }
}
