module pro.bravit.fleetPlugin.funCounter {
    requires fleet.frontend;
    requires fleet.kernel;
    requires fleet.noria.ui;
    requires fleet.rhizomedb;
    requires fleet.frontend.ui;
    requires fleet.frontend.lang;
    requires kotlin.parser;

    exports pro.bravit.fleetPlugin.funCounter;
    provides fleet.kernel.plugins.Plugin with pro.bravit.fleetPlugin.funCounter.FunCounter;
}