import Title from "../components/title";
import EndpointField from "../components/endpointField";
import {useContext, useState} from "react";
import Field from "../components/field";
import Submit from "../components/submit";
import {BaseUrl, Context} from "../App";
import axios from "axios";
import {toast} from "react-toastify";
import PoList from "./po-list";


function transformItem(item) {
  return [item.item_id, item.item_name];
}
function transformWarehouse(warehouse) {
  return [warehouse.ware_id, warehouse.ware_name];
}

function AutoPo() {
  const baseUrl = useContext(BaseUrl)
  const {page, setPage} = useContext(Context)
  const [item, setItem] = useState(1);
  const [warehouseId, setWarehouseId] = useState(1);
  const [quantity, setQuantity] = useState(1);

  async function submit() {
    try {
      const po = await axios.put(baseUrl + "/autopurchaseorder", {
        item_id: item,
        quantity: quantity,
      });
      console.log(po.data)
      setPage(<PoList po={po.data} warehouse={warehouseId} />)
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Auto PO</Title>
      <EndpointField  text1={"Item"} endpoint={"/items"} transform={transformItem} onValueChange={setItem}/>
      <EndpointField text1="Warehouse:" endpoint={"/warehouses"} transform={transformWarehouse} onValueChange={setWarehouseId}/>
      <Field editable={true} text1={"Quantity" } text2={quantity} onValueChange={setQuantity}/>
      <Submit onClick={submit}/>
    </>
  );
}

export default AutoPo;