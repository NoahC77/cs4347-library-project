import { useState, useContext } from 'react'
import { Context } from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import Ware from './ware'
import AddWare from './add-ware'
import ListPage from "../components/list-page";


function Wares() {
  const { page, setPage } = useContext(Context)

  return (
    <ListPage
      title="Warehouses"
      getEndpoint="/warehouses"
      searchEndpoint="/warehouseSearch"
      transform={(ware) => <Option
        key={ware.ware_id}
        text1={ware.ware_name}
        text2={`${ware.city}, ${ware.state}`}
        className2="text-right"
        onClick={() => setPage(<Ware
          name={ware.ware_name}
          wareid={ware.ware_id}
          sqft={ware.sqft}
          state={ware.state}
          city={ware.city}
          street={ware.street}
          apt={""}
        />)}
      />}
      addPage={<AddWare/>}
    />

  );
}

export default Wares;
