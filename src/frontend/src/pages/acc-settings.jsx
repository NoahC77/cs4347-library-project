import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'
import Delete from '../components/delete'

import {useState, createContext, useContext, useEffect} from 'react'
import {BaseUrl, Context, Token} from '../App'
import axios from "axios";
import {toast} from "react-toastify";

function AccSettings(props) {
  const { page, setPage } = useContext(Context)
  const [token, setToken] = useContext(Token)
  const [userData, setUserData] = useState({})
  const baseUrl = useContext(BaseUrl)
  useEffect(() => {
    async function fetchUserInfo() {
      try{
        const response = await axios.get(baseUrl + "/accountSettings", {
          headers: {
            "Authorization": token
          }
        });
        console.log(response.data)
        setUserData(response.data)
      }catch (e) {
        toast.error("Error")
      }
    }
    fetchUserInfo().then(r => {})
  },[]);

  function updateKey(key, value) {
    setUserData({...userData, [key]: value})
  }

  async function submit() {
    try{
      const response = await axios.put(baseUrl + "/updateAccount", userData, {
        headers: {
          "Authorization": token
        }
      });
      toast.success("Success")
    }catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Account Settings</Title>

      <Field editable={true} text1="Username:" text2={userData.username ?? ""} onValueChange={(val)=>updateKey("username",val)}/>
      <Field editable={true} text1="Password:" text2={userData.password ?? ""} onValueChange={(val)=>updateKey("password",val)}/>

      <Field editable={true} text1="First Name:" text2={userData.fname ?? ""} onValueChange={(val)=>updateKey("fname",val)}/>
      <Field editable={true} text1="Last Name:" text2={userData.lname ?? ""} onValueChange={(val)=>updateKey("lname",val)}/>
      <Field editable={false} text1="Job Title:" text2={userData.job_title?? ""} onValueChange={(val)=>updateKey("job_title",val)}/>

      <Field editable={true} text1="State:" text2={userData.state ?? ""} onValueChange={(val)=>updateKey("state",val)}/>
      <Field editable={true} text1="City:" text2={userData.city ?? ""} onValueChange={(val)=>updateKey("city",val)}/>
      <Field editable={true} text1="Zip Code:" text2={userData.zip_code ?? ""} onValueChange={(val)=>updateKey("zip_code",val)}/>
      <Field editable={true} text1="Street:" text2={userData.street ?? ""} onValueChange={(val)=>updateKey("street",val)}/>
      <Field editable={true} text1="Apartment:" text2={userData.apt_code ?? ""} onValueChange={(val)=>updateKey("apt_code",val)}/>

      <Submit onClick={submit}/>
    </>
  );
}

export default AccSettings;
