package raid24contribution.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.modeltransformer.ModelTransformer;
import raid24contribution.engine.nodetransformer.DoNothingTransformer;
import raid24contribution.engine.nodetransformer.NodeTransformer;
import raid24contribution.engine.typetransformer.KnownTypeTransformer;
import raid24contribution.engine.util.IOUtil;


/**
 * This factory is a static singleton that provides the whole system with classes which are
 * introduced through configuration files. This way of decoupling allows for a great flexibility in
 * those parts of the system which where classes are assigned to external constructs, such as
 * transformer classes to XML KaSCPar nodes.
 * <p>
 * By using the methods defined in this class, the system engine itself is able to perform the whole
 * transformation process without knowing any of the specialized classes that actually run it.
 * 
 */
public class TransformerFactory {
    
    private static Logger logger = LogManager.getLogger(TransformerFactory.class.getName());
    
    /**
     * Name of the folder where to all configuration files, relative to the class base folder.
     */
    public static String CONFIG_FOLDER = "config/";
    
    /**
     * Path to the properties folder, containing all properties for the SysCIR.
     */
    public static String PROPERTIES_FOLDER = CONFIG_FOLDER + "properties/";
    
    /**
     * Path to the implementation folder for the known types. Every known type implementation is located
     * in a subfolder matching its name.
     */
    public static String IMPLEMENTATION_FOLDER = CONFIG_FOLDER + "implementation/";
    
    /**
     * Name of the transformer definition file, that contains XML-node -&gt; transformer class name
     * mappings.
     */
    public static final String NODETRANSFORMERS_DEFINITION_FILE = "nodetransformers.properties";
    
    /**
     * Name of the transformer definition file, contains all active model transformers.
     */
    public static final String MODELTRANSFORMERS_DEFINITION_FILE = "modeltransformers.properties";
    
    /**
     * Name of the types definition file, that contains links to classes and other files that give
     * reference implementations of special known SystemC types. See documentation for detains on how to
     * create such a reference implementation.
     */
    private static final String TYPES_DEFINITION_FILE = "types.properties";
    
    /**
     * Name of the simple types definition file. It contains the names of all simple C++ and SystemC
     * base types which can be transformed to 'int' during the transformation without further effort.
     */
    private static final String SIMPLE_TYPES_DEFINITION_FILE = "simpletypes.properties";
    
    /**
     * Map that stores the XML node name -&gt; Transformer class mappings.
     */
    private Map<String, NodeTransformer> nodeTransformers;
    
    /**
     * Map that stores the known type name -&gt; type implementation class and helper files mappings.
     */
    private Map<String, KnownTypeTransformer> types;
    
    /**
     * List of simple type names.
     */
    private List<String> simpleTypes;
    
    /**
     * Properties file that contains all valid property mappings of the transformer definition file.
     */
    private Properties nodetranscfg;
    
    /**
     * Properties file that contains all valid property mappings of the types definition file.
     */
    private Properties typecfg;
    
    /**
     * Lazy notation of {@link #initialize(String, String, String, String)}. Uses the default names of
     * the configuration files and folders.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void initialize() throws FileNotFoundException, IOException {
        File f = new File(CONFIG_FOLDER);
        if (f.exists() && f.isDirectory()) {
            initialize(PROPERTIES_FOLDER + NODETRANSFORMERS_DEFINITION_FILE,
                    PROPERTIES_FOLDER + MODELTRANSFORMERS_DEFINITION_FILE, PROPERTIES_FOLDER + TYPES_DEFINITION_FILE,
                    PROPERTIES_FOLDER + SIMPLE_TYPES_DEFINITION_FILE);
        } else {
            logger.warn("Couldn't find config folder at {}, searching at jar location", CONFIG_FOLDER);
            changeConfigLocationToJarLocation();
        }
    }
    
    /**
     * Static initialization method, has to be called before using any other method of this class. Reads
     * all the config files into property objects and other containers.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void initialize(String nodeTransProps, String modeltransProps, String typeProps, String simpleTypeCfg)
            throws FileNotFoundException, IOException {
        
        // node transformers
        if (Files.exists(Paths.get(nodeTransProps))) {
            this.nodeTransformers = new HashMap<>();
            this.nodetranscfg = new Properties();
            this.nodetranscfg.load(IOUtil.getInputStream(nodeTransProps));
        } else {
            File f = new File(nodeTransProps);
            logger.error("Configuration file for noden transformers does not exist at " + f.getAbsolutePath() + ".");
        }
        
        // model transformers
        if (Files.exists(Paths.get(modeltransProps))) {
            // do nothing here
        } else {
            File f = new File(modeltransProps);
            logger.error("Configuration file for model transformer does not exist at " + f.getAbsolutePath() + ".");
        }
        
        // type properties
        if (Files.exists(Paths.get(typeProps))) {
            this.types = new HashMap<>();
            this.typecfg = new Properties();
            this.typecfg.load(IOUtil.getInputStream(typeProps));
        } else {
            File f = new File(typeProps);
            logger.error("Configuration file for special types does not exist at " + f.getAbsolutePath() + ".");
        }
        
        // function properties
        if (Files.exists(Paths.get(simpleTypeCfg))) {
            this.simpleTypes = parseFile(simpleTypeCfg);
        } else {
            File f = new File(simpleTypeCfg);
            logger.error(
                    "Configuration file for simple type conversion does not exist at " + f.getAbsolutePath() + ".");
        }
    }
    
    
    /**
     * Change the default location of the configuration folder. Updates the properties and
     * implementation folder paths accordingly.
     * 
     * @param location Path to location of config folder to use
     */
    public static void changeConfigLocation(String location) {
        CONFIG_FOLDER = location;
        PROPERTIES_FOLDER = CONFIG_FOLDER + "properties/";
        IMPLEMENTATION_FOLDER = CONFIG_FOLDER + "implementation/";
    }
    
    /**
     * Set location of the configuration folder to the path of the jar file, if possible. Updates the
     * properties and implementation folder paths accordingly.
     * 
     * @param location Path to location of config folder to use
     */
    public void changeConfigLocationToJarLocation() throws IOException {
        File tmp = new File(TransformerFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String location = tmp.getParentFile().getPath() + "/config/";
        File f = new File(location);
        if (f.exists() && f.isDirectory()) {
            CONFIG_FOLDER = location;
            PROPERTIES_FOLDER = CONFIG_FOLDER + "properties/";
            IMPLEMENTATION_FOLDER = CONFIG_FOLDER + "implementation/";
            
            initialize(PROPERTIES_FOLDER + NODETRANSFORMERS_DEFINITION_FILE,
                    PROPERTIES_FOLDER + MODELTRANSFORMERS_DEFINITION_FILE, PROPERTIES_FOLDER + TYPES_DEFINITION_FILE,
                    PROPERTIES_FOLDER + SIMPLE_TYPES_DEFINITION_FILE);
        } else {
            logger.error("Couldn't find config folder at {}", location);
        }
    }
    
    /**
     * Parses the file and returns a list of strings containing one entry for every line in the file,
     * except for empty lines or lines beginning with a #.
     * 
     * @param file
     * @return
     */
    private static List<String> parseFile(String file) {
        List<String> ret = new LinkedList<>();
        
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtil.getInputStream(file)));
            
            while (reader.ready()) {
                String tName = reader.readLine();
                tName = tName.replaceAll("^[ \\s\\t]*", "");
                tName = tName.replaceAll("[ \\s\\t]*$", "");
                if (tName == "" || tName.startsWith("#") || tName.length() == 0) {
                    continue;
                }
                
                ret.add(tName);
                
            }
        } catch (Exception e) {
            logger.error("Problem encountered parsing property file {}: {}", file, e.getMessage());
        }
        
        return ret;
    }
    
    /**
     * To determine if a given type is considered a simple type (can be transformed as 'int'). Returns
     * true if so, else false.
     * 
     * @param name Name of the type to test
     * @return true if simple type, else false
     */
    public boolean isSimpleType(String name) {
        int index = name.indexOf("<");
        if (index != -1) {
            name = name.substring(0, index);
        }
        return this.simpleTypes.contains(name);
    }
    
    /**
     * Obtains and returns an implementation class for the given node name. If the transformer has
     * already been used before, it can be found in the transformers map. If it is used for the first
     * time, the referenced transformer class has to be loaded into the runtime. See
     * {@link #createTransformer(String)} for details.
     * 
     * @param nodeName
     * @return transformer implementation class for node
     */
    public NodeTransformer getNodeTransformerByName(String nodeName) {
        if (this.nodeTransformers == null || this.nodetranscfg == null) {
            logger.error("TransformerFactory not initialized!");
            return null;
        }
        NodeTransformer t = this.nodeTransformers.get(nodeName);
        if (t == null) {
            t = createTransformer(nodeName);
        }
        
        return t;
    }
    
    /**
     * Alternative notation for {@link #getNodeTransformerByName(String)}. Gets the string name out of
     * the node before obtaining the transformer.
     * 
     * @param node
     * @return
     */
    public NodeTransformer getNodeTransformer(Node node) {
        return getNodeTransformerByName(node.getNodeName());
    }
    
    /**
     * This method creates an instance of the class that is assigned to the given node name in the
     * transformers definition file. It does that by locating the class file in the disc and using a
     * java class loader for loading in into the runtime. If there occurs an error while loading the
     * Transformer, an error message is displayed and an instance the special
     * {@link DoNothingTransformer} is returned.
     * 
     * @param nodeName
     * @return
     */
    private NodeTransformer createTransformer(String nodeName) {
        
        String className = this.nodetranscfg.getProperty(nodeName);
        if (className == null || className == "") {
            return new DoNothingTransformer();
        }
        
        try {
            URL[] urls = {TransformerFactory.class.getResource("")};
            URLClassLoader cl = new URLClassLoader(urls);
            
            NodeTransformer t = (NodeTransformer) cl.loadClass(className).newInstance();
            this.nodeTransformers.put(nodeName, t);
            
            return t;
        } catch (InstantiationException e) {
            logger.error("Transformer for node type '{}' can not be instantiated. "
                    + "The classname associated to this node is '{}'.", nodeName, className);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("Transformer for node type '{}' can not be created."
                    + "The class file associated to this node, '{}', is not accessible.", nodeName, className);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.error("Transformer for node type '{}' can not be created. "
                    + "The class file associated to this node, '{}', can not be found.", nodeName, className);
            e.printStackTrace();
        }
        
        return new DoNothingTransformer();
    }
    
    /**
     * Obtains and returns an implementation class for the given internal SystemC type. If the assigned
     * type transformer has already been used before, it can be found in the functions map. If it is
     * used for the first time, the referenced type transformer class has to be loaded into the runtime.
     * See {@link #createFunction(String)} for details. Returns <code>null</code> if an error occurs.
     * 
     * @param name
     * @return
     */
    public KnownTypeTransformer getTypeTransformer(String name, Environment e) {
        if (this.types == null || this.typecfg == null) {
            logger.error("TransformerFactory not initialized!");
            return null;
        }
        KnownTypeTransformer t = this.types.get(name);
        if (t == null) {
            t = createTypeTransformer(name);
        }
        if (t != null) {
            t.createType(e);
        } else {
            logger.warn("Type {} not found. Will be ignored.", name);
        }
        return t;
    }
    
    /**
     * Creates an instance of the type transformer class that is associated with the given type name.
     * The association is obtained from the typecfg properties object, which is constructed from the
     * types definition file. That file contains two properties for each type. The first property maps
     * the type name to the type transformer class. The second property maps the type transformer class
     * name + '.impl' to an implementation file of any kind. This allows to provide the type transformer
     * with additional information about the type in an extra file. That way the type implementation can
     * be externalized in another format and not be hard-coded in java. This method reads both
     * properties, loads the type transformer into the runtime and sets the implementation file name
     * property of the transformer.
     * 
     * @param name
     * @return
     */
    private KnownTypeTransformer createTypeTransformer(String name) {
        // if type is unknown -> type not supported, return null
        String className = this.typecfg.getProperty(name);
        if (className == null || className == "") {
            return null;
        }
        
        try {
            // first create the transformer
            URL[] urls = {TransformerFactory.class.getResource("")};
            URLClassLoader cl = new URLClassLoader(urls);
            
            KnownTypeTransformer t = (KnownTypeTransformer) cl.loadClass(className).newInstance();
            this.types.put(name, t);
            
            // now find out if there is an external implementation
            String impl = this.typecfg.getProperty(name + ".impl");
            if (impl != null && impl != "") {
                t.setImplementation(getImplementation(name, impl));
            }
            
            return t;
        } catch (InstantiationException e) {
            logger.error("Transformer for node type '{}' can not be instantiated. "
                    + "The classname associated to this node is '{}'.", name, className);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("Transformer for node type '{}' can not be created."
                    + "The class file associated to this node, '{}', is not accessible.", name, className);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.error("Transformer for node type '{}' can not be created. "
                    + "The class file associated to this node, '{}', can not be found.", name, className);
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String getImplementation(String type, String impl) {
        return IMPLEMENTATION_FOLDER + type + "/" + impl;
    }
    
    public boolean isKnownType(String name) {
        String className = this.typecfg.getProperty(name);
        if (className == null || className == "") {
            return false;
        }
        
        return true;
        
    }
    
    /**
     * Returns a list of all model transformers listed in the modeltransformers property file. The
     * transformers are ordered by occurence in the file.
     * 
     * @return
     */
    public static List<ModelTransformer> getModelTransformers() {
        List<String> transformerPaths = parseFile(PROPERTIES_FOLDER + MODELTRANSFORMERS_DEFINITION_FILE);
        List<ModelTransformer> ret = new LinkedList<>();
        // Generate Classloader
        URL[] urls = {TransformerFactory.class.getResource("")};
        URLClassLoader cl = new URLClassLoader(urls);
        
        for (String path : transformerPaths) {
            // Create instance of model transformer
            try {
                ModelTransformer mt = (ModelTransformer) cl.loadClass(path).newInstance();
                ret.add(mt);
            } catch (InstantiationException e) {
                logger.error("Cannot instantiate class located at path {}.", path);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                logger.error("Cannot access class located at path {}.", path);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                logger.error("No class found in {}.", path);
                e.printStackTrace();
            }
        }
        
        return ret;
    }
    
    /**
     * Adds a new enum type to the known types map and creates a new EnumTypeTransformer.
     * 
     * @param enumName
     */
    public void addEnumType(String enumName) {
        this.simpleTypes.add(enumName);
    }
}
