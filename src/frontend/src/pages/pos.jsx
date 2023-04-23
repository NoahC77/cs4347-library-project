import {useState, useContext} from 'react'
import {Context} from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import PO from './po'
import AddPO from './add-po'
import ListPage from "../components/list-page";


function POs() {
  const { page, setPage } = useContext(Context)
  return (
    <>
      <ListPage
        title="Purchase Orders"
        getEndpoint="/purchaseOrders"
        searchEndpoint="/purchaseOrderSearch"
        transform={(po) => <Option
          key={po.po_id}
          text1={po.po_id}
          className1="text-center col-span-2"
          className2="hidden"
          onClick={() => setPage(<PO
            orderid={po.po_id}
            date={po.purchase_date}
            quantity={""}
            price={""}
          />)}

        />}
        addPage={<AddPO/>}
      />
    </>
  )

}


export default POs;
