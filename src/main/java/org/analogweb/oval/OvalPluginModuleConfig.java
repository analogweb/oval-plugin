package org.analogweb.oval;

import org.analogweb.ModulesBuilder;
import org.analogweb.PluginModulesConfig;
import org.analogweb.util.MessageResource;
import org.analogweb.util.PropertyResourceBundleMessageResource;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.analogweb.util.logging.Markers;

/**
 * <a href="http://oval.sourceforge.net/">Oval</a>フレームワークを
 * <a href="https://github.com/analogweb">Analog Web Framework</a>
 * に統合する{@link PluginModulesConfig}です。<br/>
 * このプラグインを使用することで、エントリポイント実行時に、
 * Ovalを利用したエントリポイントの引数の値の検証を行う事が可能になります。
 * @author snowgoose
 */
public class OvalPluginModuleConfig implements PluginModulesConfig {

    /**
     * Ovalプラグインで使用する{@link MessageResource}です。
     */
    public static final MessageResource PLUGIN_MESSAGE_RESOURCE = new PropertyResourceBundleMessageResource(
            "org.analogweb.oval.analog-messages");
    private static final Log log = Logs.getLog(OvalPluginModuleConfig.class);

    @Override
    public ModulesBuilder prepare(ModulesBuilder builder) {
        log.log(PLUGIN_MESSAGE_RESOURCE, Markers.BOOT_APPLICATION, "IOVB000001");
        builder.addInvocationProcessorClass(OvalInvocationProcessor.class);
        return builder;
    }

}
