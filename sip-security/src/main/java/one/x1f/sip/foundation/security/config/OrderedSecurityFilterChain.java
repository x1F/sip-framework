package one.x1f.sip.foundation.security.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

public class OrderedSecurityFilterChain implements SecurityFilterChain, Ordered {
    private final int order;
    private final SecurityFilterChain delegate;

    public OrderedSecurityFilterChain(int order, SecurityFilterChain delegate) {
        this.order = order;
        this.delegate = delegate;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return delegate.matches(request);
    }

    @Override
    public List<Filter> getFilters() {
        return delegate.getFilters();
    }
}

