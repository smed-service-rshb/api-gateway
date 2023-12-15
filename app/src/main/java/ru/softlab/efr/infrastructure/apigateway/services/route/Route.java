package ru.softlab.efr.infrastructure.apigateway.services.route;

import org.springframework.util.AntPathMatcher;

/**
 * Описание маршрута
 */
public class Route {

    private String pattern;
    private String serviceName;
    private ResourceType resourceType;

    public Route() {
    }

    /**
     * Создать описание маршрута
     *
     * @param pattern      шаблон
     * @param serviceName  имя сервиса, которому принадлежит шаблон маршрута
     * @param resourceType тип маршрута
     */
    public Route(String pattern, String serviceName, ResourceType resourceType) {
        this.pattern = pattern;
        this.serviceName = serviceName;
        this.resourceType = resourceType;
    }

    /**
     * @return шаблон маршрута. для описани используется ant-формат. Например /clients/&#42;/transfers/&#42;&#42;.
     * @see AntPathMatcher
     */
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @return имя сервиса, которому принадлежит шаблон маршрута
     */
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return тип маршрута
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public String toString() {
        return "Route{" +
                "pattern='" + pattern + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", resourceType=" + resourceType +
                '}';
    }
}
