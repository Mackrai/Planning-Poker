import React, { Component } from 'react'
import axios from "axios";
import PButton from "./components/PButton";

const URL = 'ws://localhost:8080/ws'

class Chat extends Component {
    constructor(props) {
        super(props)

        this.state = {
            sessions: []
        }

        this.createChat = this.createChat.bind(this)
        this.sessions = this.sessions.bind(this)
    }

    ws = new WebSocket(URL)

    componentDidMount() {
        this.sessions()

        this.ws.onopen = () => {
            // on connecting, do nothing but log it to the console
            console.log('connected')
        }

        this.ws.onmessage = evt => {
            // on receiving a message, add it to the list of messages
            // const message = JSON.parse(evt.data)
            const message = evt.data
            console.log(message)
        }

        this.ws.onclose = () => {
            console.log('disconnected')
            // automatically try to reconnect on connection loss
            this.setState({
                ws: new WebSocket(URL),
            })
        }
    }

    componentWillUnmount() {
        this.ws.close()
    }

    createChat() {
        axios.post(`http://localhost:8080/createChat`, {"title": "room1"})
            .then(r => console.log(r))
    }

    sessions() {
        axios.get(`http://localhost:8080/sessions`)
            .then(res => {
                const sessions = res.data.sessions;
                console.log(sessions)
                this.setState({ sessions });
            })
    }

    render() {
        return (
            <div>
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

export default Chat