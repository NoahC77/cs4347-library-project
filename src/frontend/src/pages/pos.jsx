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
          text1={`${po.vendor_name} on ${po.purchase_date}`}
          text2={`$${po.total_price}`}
          className1="text-left "
          className2="text-right"

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
