namespace('planningPoker.Host',{
    'planningPoker.HostService':"HostService",
    'planningPoker.Spinner':'Spinner',
    'planningPoker.Viewtility':'Viewtility',
    'planningPoker.VoteDisplay':'VoteDisplay'
},({ HostService, Spinner, Viewtility, VoteDisplay }) => {
    return class extends React.Component {
        constructor(props) {
            super(props);
            this.state = {};
        }
        canHost() {
            return this.state.hostNameTag && this.state.hostNameTag.length > 0;
        }
        hostSession() {
            this.setState({ loading: true });
            HostService.host(this.state.hostNameTag, (session) => {
                this.setState({ session, loading: false, hostNameTag: session.host.nameTag });
                HostService.getData(session.id, (session) => {
                    if(this.state.session.lastModified < session.lastModified) {
                        this.setState({ session, hostNameTag: session.host.nameTag });
                    }
                }, (error) => {
                    this.setState({ error });
                })
            }, (error) => {
                this.setState({ error });
            });
        }
        vote(value) {

        }
        render() {
            return <>{
                this.state.session ? <VoteDisplay guest={this.state.host} context={this.state.context} onVote={(myVote) => {
                    this.vote(value);
                }}/> : (this.state.loading ? <Spinner/> : <>
                    <h1 className="text-center">Planning Poker</h1>
                    <h2 className="text-center">Host Session</h2>
                    <div className="d-flex flex-column">
                        <div className="d-flex justify-content-center">
                            { Viewtility.buildTextField("Session Key","sessionKey",{},() => this.state.hostNameTag,(hostNameTag) => {
                                this.setState({ hostNameTag });
                            }) }
                        </div>
                        <div className="d-flex justify-content-center">
                            <button className="btn btn-success" disabled={!this.canhost()} onClick={() => {this.hostSession()}}>Host Session</button>
                        </div>
                    </div>
                </>)
            }</>;
        }
    }
});