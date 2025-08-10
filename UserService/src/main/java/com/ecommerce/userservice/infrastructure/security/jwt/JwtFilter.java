    package com.ecommerce.userservice.infrastructure.security.jwt;

    import com.ecommerce.userservice.infrastructure.service.CustomUserDetailsService;
    import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;

    import java.io.IOException;

    @Component
    public class JwtFilter extends OncePerRequestFilter{

        private final JwtUtil jwtUtil;
        private final CustomUserDetailsService userDetailsService;
        public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
            this.jwtUtil = jwtUtil;
            this.userDetailsService = userDetailsService;
        }


        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            //Login ve Register endpointlerini filtreleme, yani bu endpointlere gelen isteklerde token kontrolü yapmıyoruz
            String path = request.getServletPath();
            if (path.equals("/auth/login") || path.equals("/auth/register")) {
                filterChain.doFilter(request, response);
                return;
            }
            //Token kontrolü yapılıyor
            String email = null;
            String token = null;
            // önce auth header'dan tokeni alıyoruz ayrıca emaili de alıyoruz
            String authHeader = request.getHeader("Authorization");
            if(authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                email = jwtUtil.extractEmail(token);
            }
            // Eğer token geçerliyse ve authenticate olunmamışsa
            if(email != null && SecurityContextHolder.getContext().getAuthentication()==null){
                // token validasyonu için userdetails oluşturalım
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                // token validasyonunu yapalım
                if(jwtUtil.validateToken(token,userDetails)){
                    // Eğer token geçerliyse, SecurityContext'e userDetails'i ekleyelim
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    //authToken.setDetails(userDetails);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        }

    }
