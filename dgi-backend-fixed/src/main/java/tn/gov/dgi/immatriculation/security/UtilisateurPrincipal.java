package tn.gov.dgi.immatriculation.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.gov.dgi.immatriculation.model.Utilisateur;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UtilisateurPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String motDePasseHash;
    private final UUID contribuableId;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UtilisateurPrincipal(Utilisateur utilisateur) {
        this.id = utilisateur.getId();
        this.email = utilisateur.getEmail();
        this.motDePasseHash = utilisateur.getMotDePasseHash();
        this.contribuableId = utilisateur.getContribuableId();
        this.enabled = Boolean.TRUE.equals(utilisateur.getActif());
        // "ROLE_" prefix requis par les expressions hasRole() de Spring Security
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
    }

    @Override public String getPassword() { return motDePasseHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
