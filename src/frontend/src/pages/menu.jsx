import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import Items from './items'
import SuppItems from './supp-items'
import MakeSale from './make-sale'
import POs from './pos'
import Wares from './wares'
import Vendors from './vendors'
import SaleHist from './sale-hist'
import AccSettings from './acc-settings'
import AutoPo from "./auto-po";

function Menu() {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <Title>Menu</Title>

      <Option text1="Items" className1="text-center col-span-2" onClick={() => setPage(<Items/>)}/>
      <Option text1="Supplied Items" className1="text-center col-span-2" onClick={() => setPage(<SuppItems/>)}/>
      <Option text1="Make Sale" className1="text-center col-span-2" onClick={() => setPage(<MakeSale/>)}/>
      <Option text1="Purchase Orders" className1="text-center col-span-2" onClick={() => setPage(<POs/>)}/>

      <Option text1="Warehouses" className1="text-center col-span-2" onClick={() => setPage(<Wares/>)}/>
      <Option text1="Vendors" className1="text-center col-span-2" onClick={() => setPage(<Vendors/>)}/>
      <Option text1="Sales History" className1="text-center col-span-2" onClick={() => setPage(<SaleHist/>)}/>
      <Option text1="Account Settings" className1="text-center col-span-2" onClick={() => setPage(<AccSettings/>)}/>
      <Option text1="Auto PO" className1="text-center col-span-2" onClick={() => setPage(<AutoPo/>)}/>
    </>
  );
}

export default Menu;
