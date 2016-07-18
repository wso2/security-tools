Steps to configure the zapwso2jiraplugin from the source

1. clone the zap-extensions from the zaproxy repository 
        https://github.com/zaproxy/zap-extensions

2. Download the zapwso2jiraplugin

3. Copy the extension to the org.zaproxy.zap.extension path
        https://github.com/zaproxy/zap-extensions/tree/master/src/org/zaproxy/zap/extension
        
4. Add the zapwso2jiraplugin as an add-on in the build.xm file
    		<build-addon name="zapwso2jiraplugin"/>
    
5. Execute the ant-build operation

6. Find the zapwso2jiraplugin-alpha-1.zap inside the build/zap-exts folder after the ant-build

7. Copy the zapwso2jiraplugin-alpha-1.zap to the /home/<NAME>/.ZAP/plugin/ folder
