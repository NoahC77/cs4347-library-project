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
import UpdateAcc from './pages/update-acc'
import Vendor from './pages/vendor'
import Vendors from './pages/vendors'
import Ware from './pages/ware'
import Wares from './pages/wares'
import { useState, createContext } from 'react'

export const Context = createContext()

function App()
{
  const emptyPage = <></>
  const [page, setPage] = useState(emptyPage)

  if(page === emptyPage)
    setPage(<Menu/>)

  return (
    <>
      <main className="min-h-screen w-[100vw] bg-[#CCCCCC] overflow-x-hidden">
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
