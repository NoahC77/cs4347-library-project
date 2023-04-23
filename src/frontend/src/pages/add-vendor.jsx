import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import { useState, createContext, useContext } from 'react'
import { Context } from '../App'
import {AddPage, FieldType} from "../components/addPage";

function AddVendor(props) {
  return (
    <AddPage
      title="Add Vendor"
      endpoint="/addVendor"
      fields={[
        {key: "vendor_name", label: "Name", type: FieldType.STRING},
        {key: "city", label: "City", type: FieldType.STRING},
        {key: "state", label: "State", type: FieldType.STRING},
        {key: "street", label: "Street", type: FieldType.STRING},
        {key: "zip_code", label: "Zip Code", type: FieldType.STRING},
        {key: "apt_code", label: "Apartment", type: FieldType.STRING},
        {key: "vendor_id", defaultValue:-1, type: FieldType.HIDDEN},
      ]}
    />
  );
}

export default AddVendor;
