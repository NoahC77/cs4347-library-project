import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import {useState, createContext, useContext} from 'react'
import {BaseUrl, Context} from '../App'
import EndpointField from "../components/endpointField";
import {toast} from "react-toastify";
import axios from "axios";

function MakeSale(props) {
  const {page, setPage} = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [item_id, setItemId] = useState(0)
  const [warehouse_id, setWarehouseId] = useState(0)

  function transformWarehouse(warehouse) {
    return [warehouse.ware_id, warehouse.ware_name];
  }

  function transformItem(item) {
    return [item.item_id, item.item_name];
  }

  async function makeSale(item) {
    try {
      const result = await axios.post(baseUrl + "/addItem", item)
      toast("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Make Sale</Title>
      <EndpointField text1="Item:" endpoint={"/items"} transform={transformItem} onValueChange={setItemId}/>
      <EndpointField text1="Warehouse:" endpoint={"/warehouses"} transform={transformWarehouse} onValueChange={setWarehouseId}/>

      <Submit onClick={() => makeSale({item_id, warehouse_id})}/>
    </>
  );


}

export default MakeSale;
