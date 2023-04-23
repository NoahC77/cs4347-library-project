import {useState, useContext} from 'react'
import {Context} from '../App'

import Option from '../components/option'
import Search from '../components/search'
import Title from '../components/title'

import Vendor from './vendor'
import AddVendor from './add-vendor'
import ListPage from "../components/list-page";


function Vendors() {
  const { page, setPage } = useContext(Context)

  // return (
  //   <>
  //     <Title>Vendors</Title>
  //
  //     <Search onAddClick={() => setPage(<AddVendor/>)}/>
  //
  //     {vendors.map( elem =>
  //       <Option
  //         text1={elem.name}
  //         text2={`${elem.city}, ${elem.state}`}
  //         className2="text-right"
  //         onClick={() => setPage(<Vendor
  //           name={elem.name}
  //           vendorid={elem.vendorid}
  //           state={elem.state}
  //           city={elem.city}
  //           zip={elem.zip}
  //           street={elem.street}
  //           apt={elem.apt}
  //         />)}
  //       />
  //     )}
  //   </>
  // );

  return (
    <ListPage
      title="Vendors"
      getEndpoint="/vendors"
      searchEndpoint="/vendorSearch"
      transform={(vendor) => <Option
        key={vendor.vendor_id}
        text1={vendor.vendor_name}
        text2={`${vendor.city}, ${vendor.state}`}
        className2="text-right"
        onClick={() => setPage(<Vendor
          name={vendor.vendor_name}
          vendorid={vendor.vendor_id}
          state={vendor.state}
          city={vendor.city}
          zip={vendor.zip_code}
          street={vendor.street}
          apt={vendor.apt_code}
        />)}
      />}
      addPage={<AddVendor/>}
    />
  );
}

export default Vendors;
