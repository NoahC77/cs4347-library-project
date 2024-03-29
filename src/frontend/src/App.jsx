import AccSettings from './pages/acc-settings'
import AddItem from './pages/add-item'
import AddPO from './pages/add-po'
import AddSuppItem from './pages/add-supp-item'
import AddVendor from './pages/add-vendor'
import AddWare from './pages/add-ware'
import Item from './pages/item'
import Items from './pages/items'
import Login from './pages/login'
import MakeSale from './pages/make-sale'
import Menu from './pages/menu'
import PO from './pages/po'
import POs from './pages/pos'
import SaleHist from './pages/sale-hist'
import SuppItems from './pages/supp-items'
import Vendor from './pages/vendor'
import Vendors from './pages/vendors'
import Ware from './pages/ware'
import Wares from './pages/wares'

import Option from './components/option'
import Field from './components/field'
import Search from './components/search'
import Sumbit from './components/submit'
import Title from './components/title'
import Delete from './components/delete'

import {useState, createContext, useContext} from 'react'

import {toast, ToastContainer} from "react-toastify";
import 'react-toastify/dist/ReactToastify.css';
import axios from "axios";

export const Context = createContext()

export const Authentication = createContext()
export const Token = createContext()

export const BaseUrl = createContext(window.location.href.includes("localhost") ? "https://szyzznq8r8.execute-api.us-east-2.amazonaws.com" : "")

function App() {
  const baseUrl = useContext(BaseUrl)
  const [page, setPage] = useState(<Login/>)
  const [auth, setAuth] = useState(true)
  const token = useState("")
  const logoutStyle = "bg-[#C83A4A] w-[min(18vh,12vw)]"
  const loginStyle = "bg-[#2288BB] w-[min(15vh,10vw)]"

  let logOut = async () => {
    try {
      await axios.post(baseUrl + "/logout", {}, {
        headers: {
          Authorization: token[0]
        }
      })
      setAuth(false)
      toast.success("Success")
      token[1]("")
      setPage(<Login/>)
    } catch (e) {
      toast.error("Error")
    }

  };
  return (
    <>
      <main className="min-h-screen w-full bg-[#CCCCCC] overflow-x-hidden">
        <div className="grid grid-cols-2">
          <span className="
            h-[8vh] w-[min(12vh,8vw)] bg-[#2288BB] rounded-[5px] justify-self-start
            flex place-content-center place-items-center [cursor:pointer]"

                onClick={() => setPage(<Menu/>)}
          >
            <img src="/hamburger-icon.svg" className="h-[6vh] w-[6vw] object-contain"/>
          </span>

          <span className={`${auth ? logoutStyle : loginStyle}
            h-[8vh] rounded-[5px] justify-self-end [cursor:pointer]
            text-white text-[4vh] leading-[8vh] text-center`}

                onClick={logOut}
          >
            {auth ? "Logout" : "Login"}
          </span>
        </div>

        <Context.Provider value={{page, setPage}}>
          <Authentication.Provider value={{auth, setAuth}}>
            <Token.Provider value={token}>
              {page}
            </Token.Provider>
          </Authentication.Provider>
        </Context.Provider>
        <ToastContainer
          position="top-center"
          autoClose={2500}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme="light"
        />
      </main>
    </>
  )
}

export default App;
