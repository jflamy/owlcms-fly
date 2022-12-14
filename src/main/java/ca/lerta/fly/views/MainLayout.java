package ca.lerta.fly.views;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;

import ca.lerta.fly.components.appnav.AppNav;
import ca.lerta.fly.components.appnav.AppNavItem;
import ca.lerta.fly.data.entity.User;
import ca.lerta.fly.security.AuthenticatedUser;
import ca.lerta.fly.views.about.AboutView;
import ca.lerta.fly.views.apps.AppsView;
import ca.lerta.fly.views.bundles.BundlesView;
import ca.lerta.fly.views.flylogin.FlyLoginOpenerView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    static private MainLayout current = null;

    private Component drawer;

    private com.vaadin.flow.component.html.Section section;

    public static MainLayout getCurrent() {
        return current;
    }

    public static void setCurrent(MainLayout current) {
        MainLayout.current = current;
    }

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        drawer = createDrawerContent();
        addToDrawer(drawer);
        setDrawerOpened(false);
        current = this;
    }

    public void recomputeDrawer() {
        section.getElement().removeAllChildren();
        createDrawerContent();
    }

    private Component createHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassNames("view-toggle");
        toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames("view-title");

        Header header = new Header(toggle, viewTitle);
        header.addClassNames("view-header");
        return header;
    }

    private Component createDrawerContent() {
        H2 appName = new H2("owlcms-fly");
        appName.addClassNames("app-name");
        if (section == null) {
            section = new com.vaadin.flow.component.html.Section(appName,
                    createNavigation(), createFooter());
        } else {
            section.add(appName, createNavigation(), createFooter());
        }
        section.addClassNames("drawer-section");
        return section;
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();
        nav.addClassNames("app-nav");

        if (accessChecker.hasAccess(FlyLoginOpenerView.class)) {
            nav.addItem(new AppNavItem("Account Login/Logout", FlyLoginOpenerView.class, "lab la-fly"));
        }
        if (accessChecker.hasAccess(BundlesView.class)) {
            nav.addItem(new AppNavItem("Application Bundles", BundlesView.class, "la la-columns"));
        }
        if (accessChecker.hasAccess(AppsView.class)) {
            nav.addItem(new AppNavItem("Applications", AppsView.class, "la la-columns"));
        }
        if (accessChecker.hasAccess(AboutView.class)) {
            nav.addItem(new AppNavItem("About", AboutView.class, "la la-file"));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        layout.addClassNames("app-nav-footer");

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
            avatar.addClassNames("me-xs");

            ContextMenu userMenu = new ContextMenu(avatar);
            userMenu.setOpenOnClick(true);
            userMenu.addItem("Logout", e -> {
                authenticatedUser.logout();
            });

            Span name = new Span(user.getName());
            name.addClassNames("font-medium", "text-s", "text-secondary");

            layout.add(avatar, name);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
