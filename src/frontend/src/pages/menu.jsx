import { useState, useContext } from 'react'
import { Context } from '../App'
import Option from '../components/option'
import Search from '../components/search'
import Items from './items'
import SuppItems from './supp-items'
import MakeSale from './make-sale'
import PO from './po'
import Wares from './wares'
import Vendors from './vendors'
import SaleHist from './sale-hist'
import AccSettings from './acc-settings'

function Menu() {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <div className="h-[9vh] text-center font-bold text-[4vh]">
        Menu
      </div>

      <Option name="Items" className1="text-center col-span-2" onClick={() => setPage(<Items/>)}/>
      <Option name="Supplied Items" className1="text-center col-span-2" onClick={() => setPage(<SuppItems/>)}/>
      <Option name="Make Sale" className1="text-center col-span-2" onClick={() => setPage(<MakeSale/>)}/>
      <Option name="Purchase Orders" className1="text-center col-span-2" onClick={() => setPage(<PO/>)}/>

      <Option name="Warehouses" className1="text-center col-span-2" onClick={() => setPage(<Wares/>)}/>
      <Option name="Vendors" className1="text-center col-span-2" onClick={() => setPage(<Vendors/>)}/>
      <Option name="Sales History" className1="text-center col-span-2" onClick={() => setPage(<SaleHist/>)}/>
      <Option name="Account Settings" className1="text-center col-span-2" onClick={() => setPage(<AccSettings/>)}/>
    </>
  );
}

export default Menu;
