namespace('planningPoker.Guest',{
    'planningPoker.GuestService':'GuestService',
    'planningPoker.Spinner':'Spinner',
    'planningPoker.Viewtility':'Viewtility',
    'planningPoker.VoteDisplay':'VoteDisplay'
},({ GuestService, Spinner, Viewtility, VoteDisplay }) => {
    return class extends React.Component {
        constructor(props) {
            super(props);
            this.state = {};
        }
        canJoin() {
            return this.state.joinSessionKey && this.state.joinNameTag && this.state.joinSessionKey.length > 0 && this.state.joinNameTag.length > 0
        }
        joinSession() {

        }
        vote(value) {

        }
        render() {
            return <>{
                this.state.session ? <VoteDisplay guest={this.state.guest} context={this.state.context} onVote={(myVote) => {
                    this.vote(value);
                }}/> : (this.state.joining ? <Spinner/> : <>
                    <h1 className="text-center">Planning Poker</h1>
                    <h2 className="text-center">Join Session</h2>
                    <div className="d-flex flex-column">
                        <div className="d-flex justify-content-center">
                            { Viewtility.buildTextField("Session Key","sessionKey",{},() => this.state.joinSessionKey,(joinSessionKey) => {
                                this.setState({ joinSessionKey });
                            }) }
                        </div>
                        <div className="d-flex justify-content-center">
                            { Viewtility.buildTextField("Session Key","sessionKey",{},() => this.state.joinNameTag,(joinNameTag) => {
                                this.setState({ joinNameTag });
                            }) }
                        </div>
                        <div className="d-flex justify-content-center">
                            <button className="btn btn-success" disabled={!this.canJoin()} onClick={() => {this.joinSession()}}>Join Session</button>
                        </div>
                    </div>
                </>)
            }</>;
        }
    }
});