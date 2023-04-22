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

import { useState, createContext } from 'react'

export const Context = createContext()

export const BaseUrl = createContext(window.location.href.includes("localhost") ? "https://szyzznq8r8.execute-api.us-east-2.amazonaws.com" : "")

function App()
{
  const [page, setPage] = useState(<Menu/>)

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

          <span className="
            h-[8vh] w-[min(15vh,10vw)] bg-[#2288BB] rounded-[5px] justify-self-end
            text-white text-[4vh] leading-[8vh] text-center [cursor:pointer]"

            onClick={() => setPage(<Login/>)}
          >
            Login
          </span>
        </div>
          <Context.Provider value={{page, setPage}}>
            {page}
          </Context.Provider>
      </main>
    </>
  )
}

export default App;
