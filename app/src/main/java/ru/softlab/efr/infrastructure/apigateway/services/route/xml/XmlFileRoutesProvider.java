package ru.softlab.efr.infrastructure.apigateway.services.route.xml;

import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import ru.softlab.efr.infrastructure.apigateway.services.route.ResourceType;
import ru.softlab.efr.infrastructure.apigateway.services.route.Route;
import ru.softlab.efr.infrastructure.apigateway.services.route.RoutesProvider;
import ru.softlab.efr.infrastructure.apigateway.services.route.xml.generated.ObjectFactory;
import ru.softlab.efr.infrastructure.apigateway.services.route.xml.generated.RouteType;
import ru.softlab.efr.infrastructure.apigateway.services.route.xml.generated.Routes;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Поставщик правил маршрутизации запросов на основе xml-файла
 */
public class XmlFileRoutesProvider implements RoutesProvider {
    private Collection<Route> routes;

    /**
     * Конструктор
     *
     * @param resource ресурс с данныим маршрутов
     * @throws IOException в случае ошибок
     */
    public XmlFileRoutesProvider(Resource resource) throws IOException {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(ObjectFactory.class.getPackage().getName());
        try (InputStream is = resource.getInputStream()) {
            routes = createRoutes((Routes) marshaller.unmarshal(new StreamSource(is)));
        }
    }

    @Override
    public Collection<Route> getRoutes() {
        return routes;
    }

    private static Collection<Route> createRoutes(Routes routes) {
        List<Route> result = new ArrayList<>();
        if (routes == null) {
            return result;
        }
        if (routes.getRoutes() == null) {
            return result;
        }
        for (RouteType routeType : routes.getRoutes()) {
            result.add(createRoute(routeType));
        }
        return result;
    }


    private static Route createRoute(RouteType routeType) {
        return new Route(routeType.getPattern(), routeType.getService(), ResourceType.valueOf(routeType.getResourceType().value()));
    }
}
