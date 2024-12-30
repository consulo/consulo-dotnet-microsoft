package consulo.microsoft.dotnet.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.microsoft.icon.MicrosoftDotNetIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 03-Jul-22
 */
@ExtensionImpl
public class MicrosoftDotNetModuleExtensionProvider implements ModuleExtensionProvider<MicrosoftDotNetModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "microsoft-dotnet";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO(".NET Framework");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return MicrosoftDotNetIconGroup.dotnet();
	}

	@Nonnull
	@Override
	public ModuleExtension<MicrosoftDotNetModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MicrosoftDotNetModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MicrosoftDotNetModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MicrosoftDotNetMutableModuleExtension(getId(), moduleRootLayer);
	}
}
