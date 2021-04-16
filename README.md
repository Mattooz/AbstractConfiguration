# AbstractConfiguration
AbstractConfiguration attempts to be an abstraction layer between different serialization formats, such as JSON or YAML, and different sources such as a file or database or a website,
while also being easy to use and lightweight.

## Abstraction

There are 3 main parts to this process:
- A <code>ResourceHandler</code> which handles the retrieval of the raw data. It is read as an array of bytes that can be decoded into whatever type of Object you want it to be.
- A <code>ConfigurationReader</code> which handles the processing of the data and puts it a <code>ConfigurationObject</code>. *Note that at this moment it can only process key-value pair type of serialization formats as root.*
- A <code>ConfigurationFactory</code> which combines the two steps above to create the desired type of <code>ConfigurationObject</code>, such as a <code>BasicConfigurationObject</code>.

A Configuration is formed by:
- <code>ConfigurationObject</code> which contains key-value pairs. As stated before, for the time being this type of object is the only one that can be root (as-in the main object that contain all your data)
- <code>ConfigurationList</code> which contains a list of objects.

Miscellaneous utilities:
- <code>Printer</code> which writes the object in whatever way you want it.

## Basic Usage
Using the basic JSON implementation included you can write:
```java
        JSONConfigurationReader reader = new JSONConfigurationReader();
        reader.setPrinter(new JSONPrinter());

        BasicConfigurationFactory configurationFactory = new BasicConfigurationFactory(reader);

        ConfigurationObject object = configurationFactory
                .optConfiguration(new FileResourceHandler("filepath"))
                .orElse(BasicConfigurationObject.empty());
                
        //do whatever
        
        //save
        factory.write(object);
```
