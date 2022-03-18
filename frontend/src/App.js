import React, {Component, useRef} from 'react'
import axios from 'axios'
import './App.css'

import PButton from "./components/PButton";

class App extends Component {
    constructor(props) {
        super(props)

        this.state = {
            sessions : []
        }

        this.createChat = this.createChat.bind(this)
        this.sessions = this.sessions.bind(this)
    }

    createChat() {
        axios.post(`http://localhost:8080/createChat`, {"title": "room1"}).then(r => console.log(r))
    }

    sessions() {
        axios.get(`http://localhost:8080/sessions`)
            .then(res => {
                const sessions = res.data.sessions;
                console.log(sessions)
                this.setState({ sessions });
            })
    }

    componentDidMount() { this.sessions() }

    render() {
        return (
            <div className='App'>
                <div>
                    <button className='button' onClick={this.createChat}>
                        createChat
                    </button>
                </div>
                <div>
                    <button className='button' onClick={this.sessions}>
                        sessions
                    </button>
                </div>
                <div>
                    {this.state.sessions.map((session) => <PButton session={session}/>)}
                </div>
            </div>
        )
    }
}

export default App