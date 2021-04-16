package it.niccolomattei.configuration.api;

/**
 * Basic interface for all of the configuration elements.
 * All elements must be derived from ConfigurationElement.
 */
public interface ConfigurationElement {

    /**
     * Checks if this configuration element is nested inside of another one.
     *
     * @return true or false
     */
    boolean isSection();

    /**
     * Returns the path to this section inside of parent.
     * If the parent is a ConfigurationObject it will return the key to this object,
     * otherwise it will return the index of the element in this format: [index].
     * <p>
     * If this element is root it will return null.
     * <p>
     * Note that this should only return the key for this element inside of its parent.
     * If you want a full path from root you should use {@link #getFullPath()}
     *
     * @return a string containing the key for this section inside of parent element, if this is root it should return null
     */
    String sectionPath();

    /**
     * If this element is nested inside of another one it should return its parent, otherwise null.
     *
     * @return the parent element.
     */
    ConfigurationElement getParent();

    /**
     * Sets the parent of the object.
     *
     * @param object      the ConfigurationElement that contains this element
     * @param sectionPath the key or index to this element
     */
    void setParent(ConfigurationElement object, String sectionPath);

    String string(StringBuilder builder);

    ResourceHandler getSource();

    void setSource(ResourceHandler handler);

    /**
     * Default method. Returns the full path from root to this element.
     *
     * @return a string containing the full path to this element. If element is root it will return null.
     */
    default String getFullPath() {
        ConfigurationElement ce = this;
        StringBuilder builder = new StringBuilder();

        while (ce != null) {
            if (ce.isSection()) {
                //this needs to be revisited
                if (ce != this && !(ce instanceof ConfigurationList)) builder.insert(0, '.');
                builder.insert(0, ce.sectionPath());
            }
            ce = ce.getParent();
        }

        return builder.length() == 0 ? null : builder.toString();
    }

    default String getFullPath(String key) {
        return getFullPath() != null ? getFullPath() + "." + key : key;
    }


}
