namespace('planningPoker.SharedSessionUtil',{},() => {
    const buildObj = function(defaults, args) {
        return Object.keys(args).filter((k) => k in defaults).reduce((out, k) => {
            out[k] = args[k];
            return out;
        }, defaults);
    }
    const buildContext = function(args) {
        return buildObj({
            lastUpdated: 0,
            state: ""
        }, args)
    }
    const buildHost = function(args) {
        return buildObj({
            ip: [],
            key: "",
            nameTag: "",
            lastUpdated: 0,
            state: ""
        }, args)
    }
    const buildGuest = function(args) {
        return buildObj({
            ip: [],
            key: "",
            nameTag: "",
            joined: 0,
            lastUpdated: 0,
            state: ""
        }, args)
    }
    const buildUserData = function(guestArgs, contextArgs) {
        return {
            sessionId: "",
            lastUpdated: 0,
            guest: buildGuest(guestArgs),
            context: buildContext(contextArgs)
        };
    } 
    const buildSession = function(hostArgs,contextArgs) {
        return {
            id: "",
            key: "",
            lastUpdated: 0,
            host: buildHost(hostArgs),
            context: buildContext(contextArgs),
            guests:{}
        };
    }
    return { buildContext, buildHost, buildGuest, buildSession };
});