import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'
import Menu from './menu'

import { useState, createContext, useContext } from 'react'
import {Context, Authentication, Token, BaseUrl} from '../App'
import axios from "axios";
import {toast} from "react-toastify";

function Login(props) {
  const { page, setPage } = useContext(Context)
  const { auth, setAuth } = useContext(Authentication)
  const [token, setToken] = useContext(Token)
  const baseUrl = useContext(BaseUrl)

  const [ username, setUsername ] = useState("")
  const [ password, setPassword ] = useState("")

  async function login() {
    try {
      const response = await axios.post(baseUrl + "/login", {
        username,
        password
      });
      toast.success("Success")
      setToken(response.data.token)
      setAuth(true)
      setPage(<Menu/>)
    }catch (e) {
      toast.error("Error")
    }
  }


  return (
    <>
      <Title>Login</Title>

      <Field editable={true} text1="Username:" text2={username} onValueChange={setUsername}/>
      <Field editable={true} text1="Password:" text2={password} onValueChange={setPassword}/>

      <Submit onClick={login}/>
    </>
  );
}

export default Login;
