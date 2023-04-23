import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import {useState, createContext, useContext} from 'react'
import {Context} from '../App'
import {AddPage, FieldType} from "../components/addPage";

function AddWare(props) {
  const {page, setPage} = useContext(Context)

  // return (
  //   <>
  //     <Title>Add Warehouse</Title>
  //
  //     <Field editable={true} text1="Name:" text2={`${props.name}`}/>
  //     <Field editable={true} text1="Square Footage:" text2={`${props.sqft}`}/>
  //     <Field editable={true} text1="State:" text2={`${props.state}`}/>
  //     <Field editable={true} text1="City:" text2={`${props.city}`}/>
  //     <Field editable={true} text1="Street:" text2={`${props.street}`}/>
  //
  //     <Submit/>
  //   </>
  // );

  return (
    <>
      <AddPage
        title="Add Warehouse"
        endpoint="/addWarehouse"
        fields={[
          {key: "ware_id", defaultValue: -1, type: FieldType.HIDDEN},
          {key: "ware_name", label: "Name", type: FieldType.STRING},
          {key: "sqft", label: "Square Footage", type: FieldType.INT},
          {key: "state", label: "State", type: FieldType.STRING},
          {key: "city", label: "City", type: FieldType.STRING},
          {key: "street", label: "Street", type: FieldType.STRING}
        ]}
      />
    </>
  )
}

export default AddWare;
